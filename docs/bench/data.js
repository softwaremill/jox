window.BENCHMARK_DATA = {
  "lastUpdate": 1701805564200,
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
          "id": "3f0056c264281d49371c7cec5903b963be60e2d3",
          "message": "WIP",
          "timestamp": "2023-12-05T20:41:43+01:00",
          "tree_id": "231d8fd46ff5246b78f3221c611078cd544af279",
          "url": "https://github.com/softwaremill/jox/commit/3f0056c264281d49371c7cec5903b963be60e2d3"
        },
        "date": 1701805564187,
        "tool": "jmh",
        "benches": [
          {
            "name": "org.sample.RendezvousBenchmark.channel",
            "value": 171.35994752526412,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.channel_iterative",
            "value": 160.08050200439558,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "org.sample.RendezvousBenchmark.exchanger",
            "value": 98.58836241954818,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "org.sample.RendezvousBenchmark.synchronous_queue",
            "value": 200.73369014348086,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          }
        ]
      }
    ]
  }
}