#!/usr/local/bin/perl
use Time::HiRes qw(gettimeofday usleep);

my %config = (
              IMAGEDIR => "./dummy/tmp",
              FSEVENTS => 0,
              DEBUG => 0
             );

if ($config{FSEVENTS}) {
  use IO::Select;
  use Mac::FSEvents;

  my $fs = Mac::FSEvents->new(path => $config{IMAGEDIR}, file_events => 1);
  my $fh = $fs->watch;
  my $sel = IO::Select->new($fh);

  sub react {
    my $pid = fork();

    return if $pid;

    close STDIN;
    close STDOUT;

    processImage(@_);

    exit;
  }

  while ($sel->can_read) {
    my @images = grep { /\.jpe?g$/ } map { $_->path } $fs->read_events;

    for my $image (@images) {
      react($image);
    }
  }
} else {
  while (1) {
    my $input = getLatestImage($config{IMAGEDIR});

    processImage($input) if ($input);
  }
}



## Return time of day in microseconds.

sub microTime { gettimeofday * 1000; }

## Do something (that can't be optimized away) for exactly 28 ms.

sub fixedTask {
  my $start_ms = microTime();
  my $dummy = 0;

  while (microTime() - $start_ms < 28) {
    $dummy += microTime() / 1000;
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

##  (my $output = $input) =~ s/\/tmp\/([^\/]+)$/\/output\/$1.result/;
  my $output = join(".", $input, "result");

  printf "Input: %s Output: %s\n", $input, $output;

  my $t1 = microTime();

  fixedTask();
  usleep 2000;

  my $t2 = microTime();

  open(FH, ">", "$output");
  print FH "$input: " . ($t2 - $t1) . "\n";
  close(FH);

  unlink($input);
}
