#!/usr/bin/perl
use Time::HiRes qw(gettimeofday usleep);
use POSIX qw(strftime);

my %config = (
              FSEVENTS => 0,
#              BLOB => 1,
              BLOB => 0,
              BLOB_BYTES => 2 * 1024 * 1024,
              DEBUG => 0
             );

## Parse out the data dir (eg "./dummy").

die "Aborting: expecting image dir as argument." if (scalar @ARGV != 1);

my $image_dir = join("/", $ARGV[0], "tmp");

print STDERR "Watching $image_dir\n";

## Commenting out the FSEVENTS code for now, to aovid having to install
## the dependent modules on the TX2s.

##if ($config{FSEVENTS}) {
##  use IO::Select;
##  use Mac::FSEvents;
##
##  my $fs = Mac::FSEvents->new(path => $image_dir, file_events => 1);
##  my $fh = $fs->watch;
##  my $sel = IO::Select->new($fh);
##
##  sub react {
##    my $pid = fork();
##
##    return if $pid;
##
##    close STDIN;
##    close STDOUT;
##
##    processImage(@_);
##
##    exit;
##  }
##
##  while ($sel->can_read) {
##    my @images = grep { /\.jpe?g$/ } map { $_->path } $fs->read_events;
##
##    for my $image (@images) {
##      react($image);
##    }
##  }
##} else {
  while (1) {
    my $input = getLatestImage($image_dir);

    processImage($input) if ($input);
  }
##}



## Return time of day in milliseconds.

sub milliTime { gettimeofday * 1000; }

## Return POSIX formatted timestamp.

sub getTime {
  my $t = gettimeofday;
  my $date = strftime("%Y-%m-%d %H:%M:%S", localtime($t));

  $date .= sprintf ".%03d", ($t - int($t))*1000;
  return $date;
}

## Do something (that can't be optimized away) for exactly 28 ms.

sub fixedTask {
  my $start_ms = milliTime();
  my $dummy = 0;

  while (milliTime() - $start_ms < 28) {
    $dummy += milliTime() / 1000;
  }
}

## Returns a list of available files, sorted by last modification date.

sub getFiles {
  my $dir = shift;

  opendir my $dh, $dir or die "Could not open '$dir' for reading: $!\n";

  my @files = sort {(stat $a)[9] <=> (stat $b)[9]}
    map { join("/", $dir, $_) } readdir($dh);

  close($dh);

  @files;
}

sub filterImages { grep { /\.jpe?g$/ } @_; }
sub filterJsons { grep { /\.json$/ } @_; }

sub getImages { filterImages(getFiles(@_)); }
sub getJsons { filterJsons(getFiles(@_)); }

sub getLatestImage {
  my @images = getImages(@_);

  pop @images;
}

sub processImage {
  my $input = shift;

  my $start_timestamp = getTime();
  my $output = join(".", $input, "json");

  printf STDERR "Input: %s Output: %s\n", $input, $output;

  my $t1 = milliTime();

  fixedTask();
  usleep 2000;

  my $t2 = milliTime();
  my $stop_timestamp = getTime();

  open(FH, ">", "$output");

  print FH "dummytf file: $input\n";
  print FH "dummytf start timestamp: " . $start_timestamp . "\n";
  print FH "dummytf stop timestamp: " . $stop_timestamp . "\n";
  print FH "dummytf duration: " . ($t2 - $t1) . "\n";
  print FH "dummytf blob: " . ('a' x $config{BLOB_BYTES}) . "\n" if ($config{BLOB});
  print FH "\n";

  close(FH);

  unlink($input);
}
