#!/usr/local/bin/perl

use strict;
use Time::HiRes qw(usleep);

my %config = (
              LOWER_THRESHOLD => 20,
              UPPER_THRESHOLD => 100,
              MAX_BATCH_SIZE => 2,
              SLEEP_MICROSECONDS => 10000,
              DELETE => 0,
              DEBUG => 0
             );

## 1. receive data and metadata comprising a new image from client via stdin
##   => CORRECTION: gets filename passed as arg!
## 2. send image to tensorflow app for processing (asynchronously)
## 3. locate the earliest available tensorflow result (from previously processed images)
## 5. send that result back to client via stdout
## 6. delete result locally
## 7. exit immediately

## Example of calling pattern:
##
##   /bin/sh /Users/mike/bin/testoc /Users/mike/tmp/ocspark/tmp/teddy.jpg

## Image flow:
##
## *  input/foo.jpg
##       [ processImages (MOVE FILE) ]
##    input/processing/foo.jpg
##       [ mvBatch (MOvE FILE) ]
##    input/completed/foo.jpg
##    tmp/foo.jpg
##       [ READ FILE -> app -> STDOUT ]
## *  output/scooby.jpg.result
##    output/1/scooby.jpg -> input/processing/completed/foo.jpg [DOESN'T EXIST!]

## The client (TfServer) writes out the image as:
##
##         .../tmp/foo.jpg
##
## then it execs the app as follows:
##
##         newapp.pl .../tmp/foo.jpg
##
## (At the same moment, Burcak's TF Server sees that image and starts
## processing it, eventually writing the result somewhere that we have
## configured (for convenience the same directory as the inputs), eg:
##
##         .../tmp/foo.jpg.json
##
## but this is happening asynchronously so we don't hae to worry about
## it for thee purposes of this flow.)
##
## The app ignores the specifed image and instead looks for the
## earliest available (or possibly all available) results in our
## specified result directory, then streams it back on STDOUT to the
## client (TfServer).
##
## The client receives the result and writes it out as:
##
##         ../output/foo.jpg.result
##
## and we're done.

## Now stream (first available) json result to STDOUT.

use File::Basename;

## Infer the image directory from the first argument: we expect
## tensorflow to put its results in that same directory.

my $dir = dirname($ARGV[0]);

## Look in the result directory for any available json files.

my @files = getFiles($dir);

##print "files: ", join("\n", sort { $a <=> $b } @files) . "\n";

my @images = filterImages(@files);
my @jsons = filterJsons(@files);

print "images: ", join(',', @images), "\n" if ($config{DEBUG});
print "jsons: ", join(',', @jsons), "\n" if ($config{DEBUG});

## Make sure we're not sending too many json files if we're in (presumed)
## steady state (ie. not too many images queued up).

if (scalar(@images) < $config{LOWER_THRESHOLD}) {
  @jsons = @jsons[0..min($config{LOWER_THRESHOLD},scalar(@jsons))-1];
}

print "jsons after lower threshold: ", join(',', @jsons), "\n" if ($config{DEBUG});

## Delete the json files straight away, to minimize chance of a
## subsequent invocation finding the same files (since there's no
## other mechanism (eg. lock file) to prevent that happening).

if ($config{DELETE}) {
##  foreach my $json (@jsons) { unlink($json); }
  foreach my $json (@jsons) { print "unlink($json)\n"; }
}

#### Read in the earliest available json file.
##
##open($fh_json, "<", join("/", $dir, $jsons[0]));

## Note: the client (TfServer) will write {stdout + stderr} to its own final
## results file (foo.jpg.result).

## Read in all available json files.  Note: each one should already contain
## its own image name (say, as "foo.jpg:blah") in its stdout.

my $content = join("|||RESULT|||", map {
  my $filename = $_;
  open(my $fh, "<", join("/", $dir, $filename));
  my $json_content = <$fh>;
  close($fh);
  (my $imagename = $filename) =~ s/\.json$//;
  "$imagename|||PAIR|||$json_content";
} @jsons);

## Now apply back pressure: wait until the number of pending images
## is below the threshold.  (For now just use a dumb loop.)

while (scalar(getImages($dir)) > $config{UPPER_THRESHOLD}) {
  usleep($config{SLEEP_MICROSECONDS});
}

## Finally emit the results and exit.

print STDOUT $content;


exit;


sub min {
  my $a = shift;
  my $b = shift;

  $a < $b ? $a : $b;
}


sub getFiles {
  my $dir = shift;

  opendir my $dh, $dir or die "Could not open '$dir' for reading: $!\n";

  my @files = readdir $dh;

  close($dh);

  @files;
}

sub filterImages { grep { ! /\.json$/ } grep { ! /^\./ } @_; }
sub filterJsons { grep { /\.json$/ } @_; }

sub getImages { filterImages(getFiles(@_)); }
sub getJsons { filterJsons(getFiles(@_)); }



## TODO: put filename insertion in here, rather than manual
## TODO: use only pipe delimiters: eg. "|||MAJORSPLIT|||" and "|||MINORSPLIT|||"
## TODO: move to 1/100 second sleep
## TODO: port to C

## Post C port:
##
## TODO: introduce lower threshold: if # of images is < lower_threshold then
## send *no more than* N json files s. t. total runtime is < 10 ms. (N will be
## inferred from production analysis).  (So will the 10ms threshold too!)
## TODO: irrespective of # of pending images files, if we see more than M pending json files then we should alert (somehow).  Probably via stderr. (CHECK THIS).

