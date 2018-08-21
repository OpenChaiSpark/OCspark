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
#              TEST_IMAGE => "/Users/mike/tmp/cat.jpg",
              PERIOD => 100,   # milliseconds
#              PERIOD => 5000,   # milliseconds
              ANALYSE => 1,
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
my $total_results = 0;
my $total_proc_time = 0;
my $total_total_time = 0;
my $total_delay_time = 0;
my $total_pending_results = 0;
my $total_pending_images = 0;

print STDERR "Writing to $input_dir analysing $output_dir\n";

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


sub parseResultLine {
  my $line = shift;

  chomp $line;

  substr $line, rindex($line, ': ') + length ': ';
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
  my @new_results = @results[$total_results..$#results];
  my $n_new_results = scalar(@new_results);

  my $finish_time;
  my $delta_time;

  if ($config{ANALYSE}) {
    ## Parse data from the new results.  Example:
    ##
    ##   /data/tmp/test3.jpg: 30.087158203125
    ##   total time (us): 266
    ##   delay time (us): 27
    ##   pending results: 0
    ##   pending images: 1

    my $filename;
    my $proc_time;
    my $total_time;
    my $delay_time;
    my $pending_results;
    my $pending_images;

    foreach my $result (@new_results) {
      open(FH, $result) || die "Error: $!\n";
      my @lines = <FH>;
      chomp $lines[0];
      ($filename, $proc_time) = split(": ", $lines[0]);
      $total_time = parseResultLine($lines[1]);
      $delay_time = parseResultLine($lines[2]);
      $pending_results = parseResultLine($lines[3]);
      $pending_images = parseResultLine($lines[4]);

      ## Aggregate.

      $total_results ++;

      $total_proc_time += $proc_time;
      $total_total_time += $total_time;
      $total_delay_time += $delay_time;
      $total_pending_results += $pending_results;
      $total_pending_images += $pending_images;

      my $mean_proc_time = $total_proc_time / $total_results;
      my $mean_total_time = $total_total_time / $total_results;
      my $mean_delay_time = $total_delay_time / $total_results;
      my $mean_pending_results = $total_pending_results / $total_results;
      my $mean_pending_images = $total_pending_images / $total_results;

      ## Update the time tracking.

      $finish_time = milliTime;
      $delta_time = $finish_time - $start_time;

      ## Output to logfile.

      print STDERR "n=$image_counter p=$period_ms d=$delta_time tr=$total_results nr=$n_new_results pt=$proc_time tt=$total_time dt=$delay_time pr=$pending_results pi=$pending_images mpt=$mean_proc_time mtt=$mean_total_time mdt=$mean_delay_time mpr=$mean_pending_results mpi=$mean_pending_images\n";
    }
  } else {
    $total_results += $n_new_results;

    ## Update the time tracking.

    $finish_time = milliTime;
    $delta_time = $finish_time - $start_time;

    print STDERR "n=$image_counter p=$period_ms d=$delta_time tr=$total_results nr=$n_new_results\n";
  }

  ## Sleep for the rest of the period.

  my $remaining_time = $period_ms - $delta_time;

  milliSleep($remaining_time) if $remaining_time > 0;
}
