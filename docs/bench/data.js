window.BENCHMARK_DATA = {
  "lastUpdate": 1701785050853,
  "repoUrl": "https://github.com/softwaremill/jox",
  "entries": {
    "Benchmark": [
      {
        "commit": {
          "author": {
            "email": "adam@warski.org",
            "name": "adamw",
            "username": "adamw"
          },
          "committer": {
            "email": "adam@warski.org",
            "name": "adamw",
            "username": "adamw"
          },
          "distinct": true,
          "id": "0b50d5187ee6dbcb857343f583ecab580970aabb",
          "message": "WIP",
          "timestamp": "2023-12-05T15:03:40+01:00",
          "tree_id": "b3ef7008300d89ade5710accbcbebb5815003257",
          "url": "https://github.com/softwaremill/jox/commit/0b50d5187ee6dbcb857343f583ecab580970aabb"
        },
        "date": 1701785050840,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 10647.173032589522,
            "unit": "ops/ms",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 23266.461774908337,
            "unit": "ops/ms",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 8906.81548591961,
            "unit": "ops/ms",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          }
        ]
      }
    ]
  }
}