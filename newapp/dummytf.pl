#!/usr/local/bin/perl

use IO::Select;
use Mac::FSEvents;
use Time::HiRes qw(gettimeofday usleep);

my $fs = Mac::FSEvents->new(path => './dummy/tmp', file_events => 1);
my $fh = $fs->watch;
my $sel = IO::Select->new($fh);

while ($sel->can_read) {
  my @events = $fs->read_events;

  for my $event (@events) {
    my $input = $event->path;

    if ($input =~ /\.jpg$/) {
##      (my $output = $input) =~ s/\/tmp\/([^\/]+)$/\/output\/$1.result/;
      my $output = join(".", $input, "result");

      printf "Input: %s Output: %s\n", $input, $output;

      react($input, $output);
    }
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

sub react {
  my $input = shift;
  my $output = shift;

  my $pid = fork();

  return if $pid;

  close STDIN;
  close STDOUT;

  my $t1 = microTime();

  fixedTask();
  usleep 2000;

  my $t2 = microTime();

  open(FH, ">", "$output");
  print FH "elapsed: " . ($t2 - $t1) . "\n";
  close(FH);

  exit;
}

