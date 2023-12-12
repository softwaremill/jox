window.BENCHMARK_DATA = {
  "lastUpdate": 1702387160090,
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
      },
      {
        "commit": {
          "author": {
            "email": "adam@warski.org",
            "name": "Adam Warski",
            "username": "adamw"
          },
          "committer": {
            "email": "noreply@github.com",
            "name": "GitHub",
            "username": "web-flow"
          },
          "distinct": true,
          "id": "4d81568193658fe9e6825319cd8bc70a9c807e19",
          "message": "Buffered channels (#7)",
          "timestamp": "2023-12-12T13:51:46+01:00",
          "tree_id": "04a9f0643181bcb5699c0bf071639c84655327e0",
          "url": "https://github.com/softwaremill/jox/commit/4d81568193658fe9e6825319cd8bc70a9c807e19"
        },
        "date": 1702387160074,
        "tool": "jmh",
        "benches": [
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1224.4713093882701,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 241.95951363302555,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 146.93362399483564,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 208.82244410331703,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 141.61596760482536,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 129.51321658128023,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 198.82521178111116,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 141.3658649457143,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 127.13297355627722,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 193.0254418299597,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 163.07414730235044,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 95.84132995008261,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 206.07487140716796,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 144.23922764000002,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 115.49700921394339,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.60164952929607,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.14828386471241,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          }
        ]
      }
    ]
  }
}