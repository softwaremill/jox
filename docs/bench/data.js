window.BENCHMARK_DATA = {
  "lastUpdate": 1701792170009,
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
          "id": "05a5021efe60047e8b768fce185ae47931c2e4fe",
          "message": "WIP",
          "timestamp": "2023-12-05T16:58:29+01:00",
          "tree_id": "fb7af2c89151e00ead220b4c96591d59bcf3020d",
          "url": "https://github.com/softwaremill/jox/commit/05a5021efe60047e8b768fce185ae47931c2e4fe"
        },
        "date": 1701791962309,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 197.98395097142296,
            "unit": "ns/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 85.72652897673802,
            "unit": "ns/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 221.88246319940515,
            "unit": "ns/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          }
        ]
      },
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
          "id": "a6846bcfc2ba264ef6c939a74308044f061c0381",
          "message": "WIP",
          "timestamp": "2023-12-05T17:01:52+01:00",
          "tree_id": "9ced17e6694d0c62e7301bcbc135362e5ccc6057",
          "url": "https://github.com/softwaremill/jox/commit/a6846bcfc2ba264ef6c939a74308044f061c0381"
        },
        "date": 1701792169995,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 198.86444708597418,
            "unit": "ns/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 10134085.020714287,
            "unit": "ns/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 230.98469296703888,
            "unit": "ns/op",
            "extra": "iterations: 1\nforks: 1\nthreads: 2"
          }
        ]
      }
    ]
  }
}