window.BENCHMARK_DATA = {
  "lastUpdate": 1704817875753,
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
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 171.051063156146,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 159.23445960616604,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 99.46590301293925,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 203.66309904610293,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
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
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1224.4713093882701,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 241.95951363302555,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 146.93362399483564,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 208.82244410331703,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 141.61596760482536,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 129.51321658128023,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 198.82521178111116,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 141.3658649457143,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 127.13297355627722,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 193.0254418299597,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 163.07414730235044,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 95.84132995008261,
            "unit": "ns/op",
            "extra": "iterations: 10\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 206.07487140716796,
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
          "id": "21a464a168e0bb63bdfff42a6433b7e0c90dc88c",
          "message": "Speed up benchmarks",
          "timestamp": "2023-12-12T16:31:36+01:00",
          "tree_id": "c919b9c0170bb773a20974928d7d5197d34b38bb",
          "url": "https://github.com/softwaremill/jox/commit/21a464a168e0bb63bdfff42a6433b7e0c90dc88c"
        },
        "date": 1702396226846,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1167.001781656675,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 242.33029264051797,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 146.32382387748888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 200.74571751295636,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 154.71987941923126,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 128.89720161954193,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 195.89987459151513,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 142.57188373345238,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 123.66945064025053,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 187.04570474424654,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 163.78871707136753,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 97.50455660042537,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 199.83417691290813,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
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
          "id": "fcd795fb872ab75fffc7e022cdc42c0488c1c034",
          "message": "Fix test naming",
          "timestamp": "2023-12-12T17:51:28+01:00",
          "tree_id": "5596e634f1030ec29026b8b80dfb48a3beafc2ce",
          "url": "https://github.com/softwaremill/jox/commit/fcd795fb872ab75fffc7e022cdc42c0488c1c034"
        },
        "date": 1702401010762,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1167.9277664850035,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 218.62314537793637,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 140.21800576099184,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 202.0358698598211,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 137.2828064957984,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 121.564440399437,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 198.12834607555558,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 143.11023811146825,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 116.05623691542011,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 194.1301427432577,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 167.20325645897438,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 90.47188968619234,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 204.9856305476206,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 148.06291144961654,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.48136803822822,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.60889948340701,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 149.42445935934066,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "d3d5b265c661081a562d5472b0a8cfacbc0bdf6b",
          "message": "Add stress test, add missing expandBuffer call (#8)",
          "timestamp": "2023-12-13T16:57:53+01:00",
          "tree_id": "9c8b7bd53aec796b5a3fcec4681a042ddeedffeb",
          "url": "https://github.com/softwaremill/jox/commit/d3d5b265c661081a562d5472b0a8cfacbc0bdf6b"
        },
        "date": 1702484222281,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1168.4591950078354,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 222.52599611710087,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 145.37992291545532,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 223.28946717602614,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 152.0510172852984,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 119.23137626064262,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 205.20663572,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 153.88474577619047,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 125.09345702821068,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 189.44188332132,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 165.59084724766902,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 96.7988260696449,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 196.44564897054175,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.00676541130606,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.31508666193937,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.739077184928338,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 148.4062592970696,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "89ef7e1b54ce122634d2bb40bdcd65a7f9e0c13f",
          "message": "Add deploy profile",
          "timestamp": "2023-12-18T17:21:22+01:00",
          "tree_id": "c46f722206a3145cfc3c505bd58934cd0121241e",
          "url": "https://github.com/softwaremill/jox/commit/89ef7e1b54ce122634d2bb40bdcd65a7f9e0c13f"
        },
        "date": 1702917722157,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1145.9882633027214,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 230.05143043783164,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 148.9199744647137,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 216.30054494251894,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 181.60706853639925,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 157.71955688000668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 198.88669172888888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 173.62090278667443,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 157.235613301917,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 183.72219768571466,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 175.53289324188034,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 100.61646365552605,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 203.00967853933665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.64800416268777,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.15184426471544,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.999848296296292,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 147.28461908095238,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "a92a1717dd5eb78efb061b641428c0779cf45548",
          "message": "README",
          "timestamp": "2023-12-18T19:28:29+01:00",
          "tree_id": "4744fbbdaa3cf369b6cba2d1b21f09457a59e1b4",
          "url": "https://github.com/softwaremill/jox/commit/a92a1717dd5eb78efb061b641428c0779cf45548"
        },
        "date": 1702925338083,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1200.94677019116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 223.64445438782178,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 153.02887834672867,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 221.81299121065607,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 185.91034854088582,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 148.2467677292731,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 208.17019090757577,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 189.25225266427347,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 147.07828495924298,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 194.07197075459206,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 184.98629408484845,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 97.63218227193512,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 203.2516165794247,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 107.92735409649121,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.89057783396537,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 33.80565483514905,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 143.78689311301588,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "e32d0f9c612a1c5ae0692e2da29178f5d3850871",
          "message": "Fixes",
          "timestamp": "2023-12-19T09:45:48+01:00",
          "tree_id": "d0c5487ba3707033171d43d522a52bd33e214fe0",
          "url": "https://github.com/softwaremill/jox/commit/e32d0f9c612a1c5ae0692e2da29178f5d3850871"
        },
        "date": 1702976779543,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1252.7958927439597,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 247.90211371363213,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 147.91108607108896,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 207.30107078500902,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 180.56537597031522,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 156.92041962026488,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 202.96635070278168,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 169.94600597393165,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 152.5142713597222,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 198.3914138332995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 182.14265280101012,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 93.65679434041186,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 196.89753048234982,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 114.3989766828116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.17820110979154,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.701432189153444,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 146.24982116539684,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "24dc664d5d4ba04c5a5340f36fde91e5451c4828",
          "message": "Readme",
          "timestamp": "2023-12-19T20:42:01+01:00",
          "tree_id": "0d9a8b855ef0e6661660cfd166f853b68878d65c",
          "url": "https://github.com/softwaremill/jox/commit/24dc664d5d4ba04c5a5340f36fde91e5451c4828"
        },
        "date": 1703016150311,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1204.103555436857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 222.64249648776416,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 152.0082476178817,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 209.0494540081219,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 192.37416379485663,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 145.34176870676018,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 211.89597629468014,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 182.604434939899,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 144.7982774478045,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 199.8031650029824,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 192.92166361858588,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 94.58638186494366,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 199.56475317811257,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.79008282962964,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.49524551773574,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.80819852292712,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 144.14776472253968,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "7f56589cacde1640b5b2f179c86b1cd07000e559",
          "message": "0.0.3 release",
          "timestamp": "2023-12-21T14:49:00+01:00",
          "tree_id": "da821b7c4df7c8f3fe878927fdc06669382d8dc9",
          "url": "https://github.com/softwaremill/jox/commit/7f56589cacde1640b5b2f179c86b1cd07000e559"
        },
        "date": 1703168204496,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1187.1103680456747,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 236.68242437686988,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 155.16206477832625,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 224.92630509272877,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 183.6031301611998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 150.19895981122454,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 200.4920073934343,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 171.02223562222224,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 151.95827771678879,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 193.05388135745702,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 188.3258423671717,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 95.58054373453697,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 200.36771409337172,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.05235295847952,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 49.72502363191058,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.52382272381977,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 140.96090893492064,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "109707a17a2131f7fef3d5b7eea55a63468b595c",
          "message": "Release drafter",
          "timestamp": "2024-01-04T14:18:34+01:00",
          "tree_id": "c62e31a5f6e41e902f36373809aa4b2f883ca0a7",
          "url": "https://github.com/softwaremill/jox/commit/109707a17a2131f7fef3d5b7eea55a63468b595c"
        },
        "date": 1704375923211,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1151.2165182672584,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 244.2836688428649,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 145.83005076019495,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 208.93743388247404,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 181.95528862713888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 141.2086216480721,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 204.4358666121212,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 171.51796893104117,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 140.6721158021461,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 185.43803617440977,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 177.59462606767678,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 91.73481481558571,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 208.17899307943568,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.channel",
            "value": 222.0338600426866,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.single_channel_iterative",
            "value": 258.9913286058201,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.two_channels_iterative",
            "value": 229.93358417826596,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 113.14968543684209,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.31537736960172,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.11638622544803,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 143.13187821015873,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_singleChannel_defaultDispatcher",
            "value": 247.49793288981482,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_twoChannels_defaultDispatcher",
            "value": 355.82850441111106,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "00712c2c4062b9dafb0d58b7d715da92a53b31bc",
          "message": "Make select clauses immutable & reusable (#26)",
          "timestamp": "2024-01-04T21:55:27+01:00",
          "tree_id": "9ab027b530e85bdcb0df5676170825f4af9cb491",
          "url": "https://github.com/softwaremill/jox/commit/00712c2c4062b9dafb0d58b7d715da92a53b31bc"
        },
        "date": 1704403364792,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1175.59989104668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 242.61619139312447,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 147.3371456398066,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 219.62645779088734,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 190.0602153941027,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 146.25720628496097,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 213.5945062066666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 180.96965726292927,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 142.88200225460318,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 189.88205131887813,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 190.3571504043434,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 98.03697066861497,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 204.86067518765327,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.channel",
            "value": 395.73945917707744,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.single_channel_iterative",
            "value": 214.66021212666672,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.two_channels_iterative",
            "value": 249.15165528650795,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 107.16416448886939,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.28155605271831,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.288147208282616,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 141.0767834504762,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_singleChannel_defaultDispatcher",
            "value": 264.3415969833333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_twoChannels_defaultDispatcher",
            "value": 363.2068223555556,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "3b25e956e84642d6fd94f85489d409d1b0912e4a",
          "message": "Implement default clauses (#27)",
          "timestamp": "2024-01-05T15:33:34+01:00",
          "tree_id": "6085bd570144a977f4953c172464b8e5c85c2c5e",
          "url": "https://github.com/softwaremill/jox/commit/3b25e956e84642d6fd94f85489d409d1b0912e4a"
        },
        "date": 1704466866579,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1212.272963999649,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 235.5060028556017,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 153.36789263136546,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 222.71961919555932,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 182.22871778754077,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 141.9593933965256,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 210.7936583104377,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 172.01573299871794,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 133.7524443553081,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 182.92128113128123,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 178.89343904545456,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 100.6979250080605,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 199.63289403890727,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.channel",
            "value": 221.60814864463052,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.single_channel_iterative",
            "value": 236.2743098512963,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.two_channels_iterative",
            "value": 305.68983421198413,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 116.39631120305012,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.389773240705644,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.25493150334528,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 202.55439544793646,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_singleChannel_defaultDispatcher",
            "value": 262.8606908083334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_twoChannels_defaultDispatcher",
            "value": 360.5772368333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "5d2095ace2dcf0e30401481ed349742d8592aebd",
          "message": "Implement unlimited channels (#29)",
          "timestamp": "2024-01-05T20:28:57+01:00",
          "tree_id": "81018abaaacb82f4b6249ad8ba4cac617f6b5543",
          "url": "https://github.com/softwaremill/jox/commit/5d2095ace2dcf0e30401481ed349742d8592aebd"
        },
        "date": 1704484573247,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1257.4257460308995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 232.22593479184485,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 150.81629207840342,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 217.7217153813417,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 170.18990535750632,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 136.9579133173908,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 210.61465544962962,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 170.9321067493784,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 141.5782006563492,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 195.51120257523996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 184.57827262474746,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 96.05789703023557,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 202.7167054507309,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.channel",
            "value": 224.42621859743005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.single_channel_iterative",
            "value": 247.07950755277778,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.two_channels_iterative",
            "value": 261.43703009047624,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.25010552514621,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.283527240572575,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.53597273464138,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 142.68464331587302,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_singleChannel_defaultDispatcher",
            "value": 260.5661329583333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_twoChannels_defaultDispatcher",
            "value": 356.7668986444444,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "a5b80c255c0f4011b6d0aa4764febf3857ce5b5f",
          "message": "Readme",
          "timestamp": "2024-01-05T21:02:24+01:00",
          "tree_id": "a0352174cde838d588ac19697876b292945fe2a5",
          "url": "https://github.com/softwaremill/jox/commit/a5b80c255c0f4011b6d0aa4764febf3857ce5b5f"
        },
        "date": 1704486577689,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1139.4823066147,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 232.6163206477845,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 149.19847763809227,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 223.14277258080716,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 181.2060643060493,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 142.88480458388452,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 201.7606601759596,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 169.42768239971696,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 132.73307624629086,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 183.22288475567183,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel_iterative",
            "value": 192.5253696511111,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 97.0305126256962,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronous_queue",
            "value": 195.40819074575896,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.channel",
            "value": 356.9499857352263,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.single_channel_iterative",
            "value": 281.83486986222226,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.two_channels_iterative",
            "value": 245.73343193193122,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 113.38238795458089,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.100918160503824,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.25479642268305,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 247.26772502063488,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_singleChannel_defaultDispatcher",
            "value": 256.57735095277775,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.sendReceiveUsingSelect_twoChannels_defaultDispatcher",
            "value": 336.177809120635,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
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
          "id": "253fc85ea5229d3bb73ed496325862a2af474b66",
          "message": "Release 0.0.4",
          "timestamp": "2024-01-09T17:14:01+01:00",
          "tree_id": "05302d8e6624163bd04e5ba48ac272808a3f85a8",
          "url": "https://github.com/softwaremill/jox/commit/253fc85ea5229d3bb73ed496325862a2af474b66"
        },
        "date": 1704817875737,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1130.2955478,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 208.58305267999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 146.20305763214287,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 217.51477708000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 177.77488623333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 149.20961915714287,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1011.51943608,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1021.0276041199999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1019.02436282,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.788630485,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.885164425000006,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.906633585,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 147.24651592,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 120.95362226,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 115.21318588,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 14.877185977142858,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.026660475,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 13.32230008,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 178.34785866666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 92.24895874696969,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 221.40346336000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 209.32871382,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 217.88800772,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 352.68536075000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 53.388804336842114,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.27374033649194,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 69.14912661000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 60.24039773,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 59.518932459999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.957474865,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.90856421,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.289308642857145,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 105.4921513,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 142.96295362,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.85449109999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.63204252,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 30.147052280000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.09247271,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 629.35077665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 236.81500076000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 365.7245438666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}