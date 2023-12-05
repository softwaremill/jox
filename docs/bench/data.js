window.BENCHMARK_DATA = {
  "lastUpdate": 1701791962323,
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
      }
    ]
  }
}