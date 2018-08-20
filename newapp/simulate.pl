#!/usr/bin/perl
use Time::HiRes qw(gettimeofday usleep);
use File::Copy;
use strict;

my %config = (
##              TEST_IMAGE => "/shared/test.jpg",
              TEST_IMAGE => "/Users/mike/tmp/cat.jpg",
              PERIOD => 1000,   # milliseconds
              DEBUG => 0
             );

## Parse out the data dir (eg "/data").

die "Aborting: expecting data dir as argument." if (scalar @ARGV != 1);

my $input_dir = join("/", $ARGV[0], "input");
my $output_dir = join("/", $ARGV[0], "output");
my $period_ms = $config{PERIOD};
my $test_image = $config{TEST_IMAGE};
my $counter = 0;

print STDERR "Watching $input_dir\n";

while (1) {
  submitImage();
}


## Return time of day in microseconds.

sub milliTime { gettimeofday * 1000 }
sub milliSleep { usleep($_[0] * 1000) }

## Submit an image and then wait out the rest of the period.

sub submitImage {
  my $start_time = milliTime;

  $counter ++;

  my $target = join("/", $input_dir, "test${counter}.jpg");

  print "input: $test_image\n";
  print "output: $target\n";

  copy($test_image, $target) or die "Copy failed: $!";

  my $finish_time = milliTime;
  my $delta_time = $finish_time - $start_time;
  my $remaining_time = $period_ms - $delta_time;

  milliSleep($remaining_time) if $remaining_time > 0;

  print "$counter $period_ms $start_time $finish_time $delta_time\n";
}
