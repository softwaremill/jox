window.BENCHMARK_DATA = {
  "lastUpdate": 1701790570797,
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
          "id": "da42f1b17842981340ae7eb89d44135ffa9bf560",
          "message": "WIP",
          "timestamp": "2023-12-05T16:35:21+01:00",
          "tree_id": "2a2b6f97acce66206190a0be40a3be295a0e27e3",
          "url": "https://github.com/softwaremill/jox/commit/da42f1b17842981340ae7eb89d44135ffa9bf560"
        },
        "date": 1701790570784,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 9809.983433455393,
            "unit": "ops/ms",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 20198.751029825304,
            "unit": "ops/ms",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 8347.924224677055,
            "unit": "ops/ms",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          }
        ]
      }
    ]
  }
}