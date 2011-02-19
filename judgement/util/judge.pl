#!/usr/bin/perl
use File::Basename;
use File::Spec;

my $judgement = File::Spec->rel2abs(dirname(dirname($0)));
my ($probid, $langid) = @ARGV;

open(OVS, "$judgement/prob/$probid/ovs.txt") || die("cannot open ovs($!)");
($timelimit, $memlimit, $scoremax, $programs_str) = <OVS>;
close(OVS);
@programs = split / /, $programs_str;

foreach my $program (@programs) {
  chomp $program;
  chdir("$judgement/current/$program") || die("chdir fail($!)");
  system("cp $judgement/util/lang/$langid/* . >&2");
  system("./compile.sh >&2");
}

