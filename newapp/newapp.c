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

#define LEN(arr) ((int) (sizeof (arr) / sizeof (arr)[0]))

char **getFilesOld(char *dir) {
  DIR *dp;
  struct dirent *ep;
  char **files = malloc(10000 * sizeof(char*)); // TODO: reallocate every 1000 or so.

  dp = opendir("./");

  if (dp != NULL) {
    int i = 0;
    while ((ep = readdir (dp))) {
      files[i++] = strdup(ep->d_name);
      puts(ep->d_name);
    }
    (void) closedir(dp);
  } else
    perror("Couldn't open the directory");

  return files;
}

void getFiles(char *dir, char ***files, int *nfiles) {
  DIR *dp;
  struct dirent *ep;
  char **x = malloc(10000 * sizeof(char*)); // TODO: reallocate every 1000 or so.
  int n = 0;

  dp = opendir("./");

  if (dp != NULL) {
    while ((ep = readdir (dp))) {
      x[n++] = strdup(ep->d_name);
      puts(ep->d_name);
    }
    (void) closedir(dp);
  } else
    perror("Couldn't open the directory");

  *files = x;
  *nfiles = n;
}

void filterJsons(char **files, int nfiles, char ***jsons, int *njsons) {
  int n = 0;
  char **found = malloc(10000 * sizeof(char*));

  for (int i=0; i<nfiles; i++) {
    char *image = files[i];
    int len = strlen(image);

    if (len >= 5 && !strcmp(&image[len-5], ".json"))
      found[n++] = strdup(image);
  }

  *jsons = found;
  *njsons = n;
}

void filterImages(char **files, int nfiles, char ***jsons, int *njsons) {
  int n = 0;
  char **found = malloc(10000 * sizeof(char*));

  for (int i=0; i<nfiles; i++) {
    char *image = files[i];
    int len = strlen(image);

    if (len > 2 && !(len >= 5 && !strcmp(&image[len-5], ".json")))
      found[n++] = strdup(image);
  }

  *jsons = found;
  *njsons = n;
}



int main(int argc, char **argv) {
  char *filename = argv[0];
  char *dir = dirname(filename);
  char **files;
  int nfiles;

  (void) getFiles(dir, &files, &nfiles);

  printf("N: %d\n", nfiles);

  char **jsons;
  int njsons;

  filterJsons(files, nfiles, &jsons, &njsons);

  char **images;
  int nimages;

  filterImages(files, nfiles, &images, &nimages);

  int i;

  printf("NJSONS: %d\n", njsons);
  
  for (i=0; i<njsons; i++)
    printf("FOUND JSON: %s\n", jsons[i]);

  printf("NIMAGES: %d\n", nimages);
  
  for (i=0; i<nimages; i++)
    printf("FOUND IMAGE: %s\n", images[i]);

  printf("hello world\n");
}
