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

char **getFiles(char *dir) {
  DIR *dp;
  struct dirent *ep;
  char **files;

  files = malloc(10000 * sizeof(char*)); // TODO: reallocate every 1000 or so.

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


int main(int argc, char **argv) {
  char *filename = argv[0];
  char *dir = dirname(filename);

  char **files = getFiles(dir);

  printf("hello world\n");
}
