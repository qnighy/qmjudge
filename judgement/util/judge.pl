#!/usr/bin/perl
use File::Basename;
use File::Spec;

my $judgement = File::Spec->rel2abs(dirname(dirname($0)));
my $current = "$judgement/current-local";
my ($probid, $langid) = @ARGV;

open(OVS, "$judgement/prob/$probid/ovs.txt") || die("cannot open ovs($!)");
my @ovs_data = <OVS>;
chomp(@ovs_data);
my ($timelimit, $memlimit, $scoremax, $programs_str) = @ovs_data;
close(OVS);
my @programs = split / /, $programs_str;

if(-d $current) {
  system("rm -r $current");
}
system("cp -r $judgement/current $current");

foreach my $program (@programs) {
  chomp $program;
  chdir("$current/$program") || die("chdir fail($!)");
  system("cp $judgement/util/lang/$langid/* . >&2");
  system("./compile.sh >&2");
}

for(my $i = 0; ; $i++) {
  my $datadir = "$judgement/prob/$probid/data/$i";
  if(not -d $datadir) { last; }
  chdir("$current");
  system("cp $datadir/* .");
  system("cp $judgement/util/default_judge/* .");
  system("./judge.sh $i $timelimit $memlimit \"$judgement/util/lang/$langid/run.txt\"");
}

