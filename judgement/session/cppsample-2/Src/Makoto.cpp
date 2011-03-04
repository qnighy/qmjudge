#include <algorithm>
#include <cstdio>
#include <vector>
#include <makoto_lib.h>
using namespace std;

int primeTest(int num) {
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
  for(int i = 0; i < (int)primes.size(); i++) {
    if(num == primes[i]) return 0;
    if(num % primes[i] == 0) return (i+1);
  }
  return 0;
}

