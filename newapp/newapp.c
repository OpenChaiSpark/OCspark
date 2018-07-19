#include <stdio.h>
#include <sys/types.h>
#include <dirent.h>
#include <libgen.h>
#include <string.h>
#include <stdlib.h>


#define LOWER_THRESHOLD 20
#define UPPER_THRESHOLD 100
#define MAX_BATCH_SIZE 2
#define SLEEP_MICROSECONDS 10000
#define DELETE 0
#define DEBUG 0

void getFiles(char *dir, char ***files, int *nfiles) {
  DIR *dp;
  struct dirent *ep;
  char **x = malloc(10000 * sizeof(char*)); // TODO: reallocate every 1000 or so.
  int n = 0;

  dp = opendir(dir);

  if (dp != NULL) {
    while ((ep = readdir (dp))) {
      x[n++] = strdup(ep->d_name);
      //      puts(ep->d_name);
    }
    (void) closedir(dp);
  } else {
    perror("Couldn't open the directory");
    exit(1);
  }

  *files = x;
  *nfiles = n;
}

void filterJsons(char **files, int nfiles, char ***jsons, int *njsons) {
  int n = 0;
  char **found = malloc(10000 * sizeof(char*));

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
  char **found = malloc(10000 * sizeof(char*));

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




int main(int argc, char **argv) {
  char *filename = argv[1];

  if (!filename) {
    perror("No filename argument");
    exit(1);
  }

  printf("filename: %s\n", filename);

  char *dir = dirname(filename);

  printf("dir: %s\n", dir);

  char **files;
  int nfiles;

  (void) getFiles(dir, &files, &nfiles);

  printf("N: %d\n", nfiles);

  char **jsons;
  int njsons;

  (void) filterJsons(files, nfiles, &jsons, &njsons);

  char **images;
  int nimages;

  (void) filterImages(files, nfiles, &images, &nimages);

  int i;

  (void) getJsons(dir, &jsons, &njsons);
  (void) getImages(dir, &images, &nimages);
  
  printf("NJSONS: %d\n", njsons);
  
  for (i=0; i<njsons; i++)
    printf("FOUND JSON: %s\n", jsons[i]);

  printf("NIMAGES: %d\n", nimages);
  
  for (i=0; i<nimages; i++)
    printf("FOUND IMAGE: %s\n", images[i]);

  printf("hello world\n");
}