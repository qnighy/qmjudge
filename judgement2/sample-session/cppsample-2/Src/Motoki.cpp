#include <algorithm>
#include <cstdio>
#include <vector>
#include <motoki_lib.h>
using namespace std;

int sayResult(int num) {
  vector<int> primes;
  static bool isPrime[100000];
  fill(isPrime, isPrime+100000, true);
  for(int i = 2; i < 100000; i++) {
    if(!isPrime[i]) continue;
    primes.push_back(i);
    if(i >= 1000) continue;
    for(int j = i*i; j < 100000; j+=i) {
      isPrime[j] = false;
    }
  }
  if(num==0) return -1;
  else return primes[num-1];
}

