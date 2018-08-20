#!/usr/bin/perl

## Note: we're using /usr/bin/perl to make sure it's available on
## the ARM servers.  Try to avoid using non-built-in modules.

## For now we'll include pulling the new results and outputing stats
## based on the results to date in the logfile.  But if that starts
## taking too long (as the number of results gets large) then we may
## have to move that (result tracking) code to a different script.
## Note: we're using a hash to locate the last-seen result in each new
## list of results, to keep the time as linear a function of the total
## number of results as possible.

use Time::HiRes qw(gettimeofday usleep);
use File::Copy;
use strict;

my %config = (
              TEST_IMAGE => "/shared/test.jpg",
##              TEST_IMAGE => "/Users/mike/tmp/cat.jpg",
#              PERIOD => 1000,   # milliseconds
              PERIOD => 5000,   # milliseconds
              DEBUG => 0
             );

## Parse out the data dir (eg "/data").

die "Aborting: expecting data dir as argument." if (scalar @ARGV != 1);

my $data_dir = $ARGV[0];
my $input_dir = join("/", $data_dir, "input");
my $output_dir = join("/", $data_dir, "output");
my $period_ms = $config{PERIOD};
my $test_image = $config{TEST_IMAGE};
my $image_counter = 0;
my $last_result = "";

print STDERR "Watching $input_dir\n";

while (1) {
  submitImage();
}


## Return time of day in microseconds.

sub milliTime { gettimeofday * 1000 }
sub milliSleep { usleep($_[0] * 1000) }



## Returns a list of available files, sorted by last modification date.

sub getFiles {
  my $dir = shift;

  opendir my $dh, $dir or die "Could not open '$dir' for reading: $!\n";

  my @files = sort {(stat $a)[9] <=> (stat $b)[9]}
    map { join("/", $dir, $_) } readdir($dh);

  close($dh);

  @files;
}

sub filterResults { grep { /\.result$/ } @_ }

sub getResults { filterResults(getFiles(@_)) }

sub getLatestImage {
  my @images = getImages(@_);

  pop @images;
}




## Submit an image and then wait out the rest of the period.

sub submitImage {
  my $start_time = milliTime;

  ## Copy the image.

  $image_counter ++;

  my $target = join("/", $input_dir, "test${image_counter}.jpg");

  copy($test_image, $target) or die "Copy failed: $!";

  ## Get all results that have appeared since the last period.

  my @results = getResults($output_dir);
  my %results = map { $results[$_] => $_ } 0..$#results;
  my $last_index = $last_result ne "" ? $results{$last_result} : -1;
  my @new_results = @results[$last_index+1..$#results];
  my $n_new_results = scalar(@new_results);

  $last_result = $results[$#results];

  ## Sleep for the rest of the period.

  my $finish_time = milliTime;
  my $delta_time = $finish_time - $start_time;
  my $remaining_time = $period_ms - $delta_time;

  milliSleep($remaining_time) if $remaining_time > 0;

  ## Output to logfile.

  print STDERR "$image_counter $n_new_results $period_ms $start_time $finish_time $delta_time\n";
}
