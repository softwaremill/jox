window.BENCHMARK_DATA = {
  "lastUpdate": 1701791316724,
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
          "id": "4d0b27d9ffe2af00b4781b0a07ee627a21cdc518",
          "message": "WIP",
          "timestamp": "2023-12-05T16:47:46+01:00",
          "tree_id": "25bb93c3fcd692e512995e56afaf29cc5f0c6d61",
          "url": "https://github.com/softwaremill/jox/commit/4d0b27d9ffe2af00b4781b0a07ee627a21cdc518"
        },
        "date": 1701791316711,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 2.2578881519229845e-7,
            "unit": "s/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 9.076529586737431e-8,
            "unit": "s/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 2.439383911313052e-7,
            "unit": "s/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          }
        ]
      }
    ]
  }
}