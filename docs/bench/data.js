window.BENCHMARK_DATA = {
  "lastUpdate": 1701809122151,
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
          "id": "46f1a97238cea53733e55d665c97045b9806e49d",
          "message": "WIP",
          "timestamp": "2023-12-05T21:39:23+01:00",
          "tree_id": "cdb40ffd7f8c21f907ce2c6c17de95a92ee71dcb",
          "url": "https://github.com/softwaremill/jox/commit/46f1a97238cea53733e55d665c97045b9806e49d"
        },
        "date": 1701809122137,
        "tool": "jmh",
        "benches": [
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 171.051063156146,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 159.23445960616604,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 99.46590301293925,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 203.66309904610293,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 145.06077753222226,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          }
        ]
      }
    ]
  }
}