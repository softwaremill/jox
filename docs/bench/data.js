window.BENCHMARK_DATA = {
  "lastUpdate": 1701793539230,
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
          "id": "85308b720d17430b5484385496fb77ad72704137",
          "message": "WIP",
          "timestamp": "2023-12-05T17:14:47+01:00",
          "tree_id": "56379544f2792f21c2198e2150088eb8c4036272",
          "url": "https://github.com/softwaremill/jox/commit/85308b720d17430b5484385496fb77ad72704137"
        },
        "date": 1701793539216,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 183.8191404571848,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 97.71603139973247,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 204.30113761142127,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          }
        ]
      }
    ]
  }
}