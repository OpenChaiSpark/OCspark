#include <stdio.h>
#include <sys/types.h>
#include <dirent.h>
#include <libgen.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <sys/time.h>

enum {
      LOWER_THRESHOLD = 20,
      UPPER_THRESHOLD = 100,
      MAX_BATCH_SIZE = 2,
      ALERTING_LIMIT = 10,
      SLEEP_MICROSECONDS = 10000,
      DELETE = 0,
      DEBUG = 0
};

struct Stats
{
  int total_time;
  int delay_time;
  int processed_results;
  int pending_results;
  int pending_images;
};

// 1. receive data and metadata comprising a new image from client via stdin
//   => CORRECTION: gets filename passed as arg!
// 2. send image to tensorflow app for processing (asynchronously)
// 3. locate the earliest available tensorflow result (from previously processed images)
// 5. send that result back to client via stdout
// 6. delete result locally
// 7. exit immediately

// Example of calling pattern:
//
//   /bin/sh /Users/mike/bin/testoc /Users/mike/tmp/ocspark/tmp/teddy.jpg

// Image flow:
//
// *  input/foo.jpg
//       [ processImages (MOVE FILE) ]
//    input/processing/foo.jpg
//       [ mvBatch (MOvE FILE) ]
//    input/completed/foo.jpg
//    tmp/foo.jpg
//       [ READ FILE -> app -> STDOUT ]
// *  output/scooby.jpg.result
//    output/1/scooby.jpg -> input/processing/completed/foo.jpg [DOESN'T EXIST!]

// The client (TfServer) writes out the image as:
//
//         .../tmp/foo.jpg
//
// then it execs the app as follows:
//
//         newapp.pl .../tmp/foo.jpg
//
// (At the same moment, Burcak's TF Server sees that image and starts
// processing it, eventually writing the result somewhere that we have
// configured (for convenience the same directory as the inputs), eg:
//
//         .../tmp/foo.jpg.json
//
// but this is happening asynchronously so we don't hae to worry about
// it for thee purposes of this flow.)
//
// The app ignores the specifed image and instead looks for the
// earliest available (or possibly all available) results in our
// specified result directory, then streams it back on STDOUT to the
// client (TfServer).
//
// The client receives the result and writes it out as:
//
//         ../output/foo.jpg.result
//
// and we're done.

void getFiles(char *dir, char ***files, int *nfiles) {
  DIR *dp;
  struct dirent *ep;
  char **x = (char**) malloc(10000 * sizeof(char*)); // TODO: reallocate every 1000 or so.
  int n = 0;

  dp = opendir(dir);

  if (dp != NULL) {
    while ((ep = readdir (dp)))
      x[n++] = strdup(ep->d_name);

    (void) closedir(dp);
  } else {
    perror("Couldn't open the directory");
    exit(1);
  }

  *files = x;
  *nfiles = n;
}

void filterJsons(char **files, int nfiles, char ***jsons, int *njsons) {
  char **found = (char**) malloc(10000 * sizeof(char*));
  int n = 0;

  for (int i=0; i<nfiles; i++) {
    char *file = files[i];
    int len = strlen(file);

    if (len >= 5 && !strcmp(&file[len-5], ".json"))
      found[n++] = strdup(file);
  }

  *jsons = found;
  *njsons = n;
}

void filterImages(char **files, int nfiles, char ***jsons, int *njsons) {
  int n = 0;
  char **found = (char**) malloc(10000 * sizeof(char*));

  for (int i=0; i<nfiles; i++) {
    char *file = files[i];
    int len = strlen(file);

    if (len > 2 && !(len >= 5 && !strcmp(&file[len-5], ".json")))
      found[n++] = strdup(file);
  }

  *jsons = found;
  *njsons = n;
}

void getJsons(char *dir, char ***jsons, int *njsons) {
  char **files;
  int nfiles;

  (void) getFiles(dir, &files, &nfiles);
  (void) filterJsons(files, nfiles, jsons, njsons);

  for (int i=0; i<nfiles; i++) free(files[i]);

  free(files);
}

void getImages(char *dir, char ***images, int *nimages) {
  char **files;
  int nfiles;

  (void) getFiles(dir, &files, &nfiles);
  (void) filterImages(files, nfiles, images, nimages);

  for (int i=0; i<nfiles; i++) free(files[i]);

  free(files);
}

int numImages(char *dir) {
  char **images;
  int nimages;

  getImages(dir, &images, &nimages);

  return nimages;
}

char *readFile(char *filename) {
  FILE *file = fopen(filename, "rb");

  fseek(file, 0, SEEK_END);

  long len = ftell(file);

  fseek(file, 0, SEEK_SET);

  char *string = (char*) malloc(len + 1);

  fread(string, len, 1, file);
  fclose(file);

  string[len] = 0;

  return string;
}

char *concat(char *left, char *sep, char *right) {
  int left_len = strlen(left);
  int sep_len = strlen(sep);
  int right_len = strlen(right);
  int len = left_len + sep_len + right_len + 1;
  char *string = (char*) malloc(len * sizeof(char));

  int string_len = snprintf(string, len, "%s%s%s", left, sep, right);

  if (string_len != len - 1) {
    perror("String concatenation failed");
    exit(1);
  }

  return string;
}

int deltaMS(struct timeval start, struct timeval stop) {
  return ((stop.tv_sec - start.tv_sec)*1000000L + stop.tv_usec) - start.tv_usec;
}

int timeMS(struct timeval start) {
  struct timeval stop;

  gettimeofday(&stop, NULL);

  return deltaMS(start, stop);
}


// Entry point.

int main(int argc, char **argv) {
  struct Stats stats;
  char *filename = argv[1];
  struct timeval total_start;

  gettimeofday(&total_start, NULL);

  if (!filename) {
    perror("No filename argument");
    exit(1);
  }

  if (DEBUG) printf("filename: %s\n", filename);

  // Infer the image directory from the first argument: we expect
  // tensorflow to put its results in that same directory.

  char *dir = dirname(filename);

  if (DEBUG) printf("dir: %s\n", dir);

  // Look in the result directory for any available json files.

  char **files;
  int nfiles;

  (void) getFiles(dir, &files, &nfiles);

  if (DEBUG) printf("N: %d\n", nfiles);

  char **jsons;
  int njsons;

  (void) filterJsons(files, nfiles, &jsons, &njsons);

  if (DEBUG) printf("njsons: %d\n", njsons);

  char **images;
  int nimages;

  (void) filterImages(files, nfiles, &images, &nimages);

  if (DEBUG) printf("nimages: %d\n", nimages);

  // Make sure we're not sending too many json files if we're in
  // (presumed) steady state (ie. not too many images queued up).

  if (nimages < LOWER_THRESHOLD) {
    int minimum = MAX_BATCH_SIZE < njsons ? LOWER_THRESHOLD : njsons;

    for (int i=minimum; i<njsons; i++) free(jsons[i]);

    njsons = minimum;
  }

  stats.processed_results = njsons;

  if (DEBUG) printf("njsons (post batching): %d\n", njsons);

  // Delete the json files straight away, to minimize chance of a
  // subsequent invocation finding the same files (since there's no
  // other mechanism (eg. lock file) to prevent that happening).

  if (DELETE)
    for (int i=0; i<njsons; i++)
      //      remove(jsons[i]);
      printf("Would have removed: %s\n", jsons[i]);

  // Note: the client (TfServer) will write {stdout + stderr} to its
  // own final results file (foo.jpg.result).

  // Read in all available json files and concatenate their confent.

  char *content = (char*) NULL;

  for (int i=0; i<njsons; i++) {
    char *json = jsons[i];
    char *json_path = concat(dir, "/", json);
    char *json_content = readFile(json_path);

    if (DEBUG) printf("Read json content: %s\n", json_content);

    free(json_path);

    // Stem the json filename (to drop off the ".json" suffix).
  
    int json_len = strlen(json);
    char *json_stem = strdup(json);

    json_stem[json_len - 5] = '\0';

    // Create the (filename, content) pair.
  
    char *json_pair = concat(json_stem, "|||PAIR|||", json_content);

    free(json_stem);

    // Append the new pair to the list of pairs.

    char *new_content = content ?
      concat(content, "|||RESULT|||", json_pair) : strdup(json_pair);

    free(json_pair);
    free(content);

    content = new_content;
  }

  // Now apply back pressure: wait until the number of pending images
  // is below the threshold.  (For now just use a dumb loop.)  Also
  // record the total time delaying.

  struct timeval delay_start;

  gettimeofday(&delay_start, NULL);

  int pending_images;

  while ((pending_images = numImages(dir)) > UPPER_THRESHOLD)
    usleep(SLEEP_MICROSECONDS);

  stats.pending_images = pending_images;
  stats.delay_time = timeMS(delay_start);

  // Last-minute check for surviving json files (re-read beacuse more
  // may have been added since last read).

  getJsons(dir, &jsons, &njsons);

  if (njsons > ALERTING_LIMIT)
    fprintf(stderr, "Alert: %d result files pending", njsons);

  stats.pending_results = njsons;

  // Record the total time.

  stats.total_time = timeMS(total_start);

  // Emit the stats.

  fprintf(stderr, "total time (us): %d\n", stats.total_time);
  fprintf(stderr, "delay time (us): %d\n", stats.delay_time);
  fprintf(stderr, "pending results: %d\n", stats.pending_results);
  fprintf(stderr, "pending images: %d\n", stats.pending_images);
  fprintf(stderr, "processed results: %d\n", stats.processed_results);

  // Finally emit the results and exit.

  printf("%s", content);
}




// Unused.

void testFileReading(char *dir) {
  char **jsons, **images;
  int njsons, nimages;

  (void) getJsons(dir, &jsons, &njsons);
  (void) getImages(dir, &images, &nimages);
  
  printf("NJSONS: %d\n", njsons);
  
  for (int i=0; i<njsons; i++)
    printf("FOUND JSON: %s\n", jsons[i]);

  printf("NIMAGES: %d\n", nimages);
  
  for (int i=0; i<nimages; i++)
    printf("FOUND IMAGE: %s\n", images[i]);
}
