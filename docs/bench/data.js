window.BENCHMARK_DATA = {
  "lastUpdate": 1709043481130,
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
          "message": "Release 0.0.5",
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
          "id": "9c43ed00e2adb6eb0d059737b16ad333c45acd82",
          "message": "Separate Sink, Source & CloseableChannel interfaces (#32)",
          "timestamp": "2024-01-22T17:41:50+01:00",
          "tree_id": "76030baf101dee3143835d38be31b58d330a2c7e",
          "url": "https://github.com/softwaremill/jox/commit/9c43ed00e2adb6eb0d059737b16ad333c45acd82"
        },
        "date": 1705942705906,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1074.4888314,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 226.92259171999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 114.75403267333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 207.9576298,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 194.61196628000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 145.0211319714286,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1009.6290988200001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1027.66302218,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1021.99670476,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.134591965000006,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.4060459,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.98408668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 135.35496745999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 129.7534345,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 111.54979054,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 10.99041721711111,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.925127942,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.791679892727274,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 182.99176993333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 86.0563335653846,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 609.4425792999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 224.42533944000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 213.08334424,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 121.30267013333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 54.44842157894736,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.811482524999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 71.65152355000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 58.344650509999994,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 62.085045109999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 14.108516949999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.372980425,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.897755468571429,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 104.22652114,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 141.804463,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.25690236,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.004262244999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.961313435,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.524698215,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 152.37528242857144,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 258.90579264999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 321.6062986,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "3160b8fee04d2853fc171455bc6112083748aa20",
          "message": "Make Channel implement AutoCloseable (#33)",
          "timestamp": "2024-01-22T18:25:09+01:00",
          "tree_id": "6105753ce5b60cf950ca0cdcaf9a47035dcbf084",
          "url": "https://github.com/softwaremill/jox/commit/3160b8fee04d2853fc171455bc6112083748aa20"
        },
        "date": 1705945288302,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1142.7528808,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 224.09818267999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 116.49947895555553,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 213.1911271266667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 183.38447853333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 144.8053330571429,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1004.7788600200001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1017.8632022599999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1033.6690757400002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.098632990000006,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.95200799,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 32.008541045,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 115.05048609999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 120.65549852,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 118.36890168,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.681348253333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.131503096,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.368288007272728,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 181.01223199999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 92.46171759393938,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 656.4167646,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 224.50413828,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 202.88040069333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 110.62291654,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 48.83203317142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.10771469375,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 70.31545767,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 56.447356219999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 58.90387084,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.7897826,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.59927271,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.85797894,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 100.94766718000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 146.19079968,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 102.17823132999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.029336615,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.317160315000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.501555834999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 147.82280005714284,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 243.08092604000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 314.6770423,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "6d3180db26d3346ec24af2b2ac5aac28f56dc782",
          "message": "Implement source views (#34)",
          "timestamp": "2024-01-23T15:39:17+01:00",
          "tree_id": "54b47d02468aec5ef1630d533a2c07b900edf549",
          "url": "https://github.com/softwaremill/jox/commit/6d3180db26d3346ec24af2b2ac5aac28f56dc782"
        },
        "date": 1706021742856,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 993.9064565000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 216.77609436000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 121.47024024444445,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 199.58583349999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 191.33581948666665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 152.87409580357144,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1015.5666350399999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1018.46547884,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1033.45881224,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.434420785000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.613918704999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 32.733960825,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 112.30399403999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 125.66700752,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 122.10225174,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 10.424851283999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.85237934,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.598860586,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 172.7704043047619,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 86.03374808205129,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 582.2262453000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 220.25618696,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 203.99058721333336,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.3308648888889,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 53.55766852631579,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 33.01952316774193,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 71.13189919999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 58.54147395,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 58.11326462000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.9977055425,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.919181822499999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.604749445714285,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 102.7186833,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 140.7056268,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.5604151,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.894944244999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 30.782880884999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.27380832,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 144.48670828571431,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 266.15951545,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 344.53393793333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "3320290970758954e350ba192b26335f6083c332",
          "message": "Select restarts when a channel for a receive clause is done (#35)",
          "timestamp": "2024-01-23T21:08:39+01:00",
          "tree_id": "0bac6539df0ff0a0332297bc761b668297125655",
          "url": "https://github.com/softwaremill/jox/commit/3320290970758954e350ba192b26335f6083c332"
        },
        "date": 1706041507707,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1085.3035984,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 178.20043863333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 112.92232558888891,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 214.63889996666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 194.10417558666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 156.6062238857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1014.45646792,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1023.3060593399999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1018.3631601000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.00073742,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 30.298817790000005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.597165705000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 114.02008575999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 110.97761326,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 105.54966854,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 10.926895146000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.834913543999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.42156327,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 181.66239073333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 90.04621727121211,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 672.8664853,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 215.77937568,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 209.34626687999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 117.17709937777776,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 49.49620468571429,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.271916467137096,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 72.3801587,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 55.966225480000006,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 57.75486188,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 14.030179588571428,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 15.320260142857142,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.611942977142855,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 102.58496772000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 140.63232934,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 96.04094731,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.858635470000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.534867560000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.528919455,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 141.4256175,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 262.0169015,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 341.755355,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "a21dd9e42dddfd60712696364faf6b36df0079c9",
          "message": "Release 0.0.5",
          "timestamp": "2024-01-23T21:29:42+01:00",
          "tree_id": "414f3160dd429cd9e7be1d9da09e3132c9c58917",
          "url": "https://github.com/softwaremill/jox/commit/a21dd9e42dddfd60712696364faf6b36df0079c9"
        },
        "date": 1706042850335,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1159.8742796000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 182.89965862857144,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 115.38558282222223,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 201.52493385333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 190.12611546,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 144.48350402857142,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1007.9049883199999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1026.66304316,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1030.62776116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.659840589999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 30.701004545,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 32.591123915,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 123.05095778,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 125.62751055999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 105.82906068,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.005007116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.89517374,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.262444902,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 178.42009616666664,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 93.31143506818182,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 656.8456578,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 203.34250938,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 215.08126980000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 111.5756115911111,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 48.20196713333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.903111975,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 68.59685028999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 57.85208752,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 62.49182773,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 14.018437707499999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.321865445,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.657429160000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 101.63484538,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 140.5000902,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 93.94976097000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.55872809,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.527864875,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.020945089999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 137.604606875,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 243.93372276,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 356.030339,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "73a9f31e2dcb5d080130c54799e802106a9a23e6",
          "message": "Update plugin",
          "timestamp": "2024-01-24T13:51:27+01:00",
          "tree_id": "47b4b1abd7854341b89d9506df07db6b2379ff51",
          "url": "https://github.com/softwaremill/jox/commit/73a9f31e2dcb5d080130c54799e802106a9a23e6"
        },
        "date": 1706101694618,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1025.0003502000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 197.19705610666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 115.60446548888888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 207.21526107333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 187.40368297333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 154.42886881785714,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1014.8608787400001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1020.52886872,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1042.72629164,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.67613284,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 30.934773085,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 32.367722005000005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 129.65218274,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 132.36089558,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 123.21857624000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.058225768888887,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.988030819333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.845534163636364,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 174.1086772,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 91.34254456818182,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 603.3846531,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 214.4976838,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 207.59251727999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 121.25226026666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.54899267,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 33.25053914860215,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 69.66654806,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 60.61821734,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 58.93584237,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.7212528825,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.53562527,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.524864037142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 102.28634192000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 138.164679,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 96.56684806999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 28.961726679999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.879967004999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.162591275,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 143.01504419285715,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 259.5727812,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 353.66521266666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "11a392d19a7bf6ed0a0eeed5aa565c0169b8b9a3",
          "message": "Add blog",
          "timestamp": "2024-01-24T20:35:42+01:00",
          "tree_id": "fc92046b507a540094b5be2cd94d0ecf06de644a",
          "url": "https://github.com/softwaremill/jox/commit/11a392d19a7bf6ed0a0eeed5aa565c0169b8b9a3"
        },
        "date": 1706125962242,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 992.8935137999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 182.8226753942857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 113.98060922222221,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 212.86675400666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 186.48629139999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 154.30713685714284,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1005.64176548,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1021.2780004399999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1020.5558154600001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.26316259,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 30.63976158,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.737798795000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 128.72441442,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 118.81486456,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 114.70001654000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 10.870627332000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 9.58763293090909,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.960123595999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 174.0910108142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 88.07249913181818,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 642.3478908,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 214.53885631333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 202.52723668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 113.85221486666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.7022227931579,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.85560773548387,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 67.11645528000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 55.81199451,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 60.51974378,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.6471800925,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 14.860810017142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.787030997142859,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 91.51149344999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 140.80841954,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.2937742,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.25983364,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.5164176,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 33.33869587666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 140.5299963,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 259.6695163,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 340.25106293333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "77e06fa8490957dabc2455592bb1d8f0492a229c",
          "message": "Java 17 compatibility (#37)",
          "timestamp": "2024-01-25T11:02:12+01:00",
          "tree_id": "70c5999328b3953a584c5b2b7f7a909911c426f2",
          "url": "https://github.com/softwaremill/jox/commit/77e06fa8490957dabc2455592bb1d8f0492a229c"
        },
        "date": 1706177915578,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1012.4860282,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 186.62093348571426,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 117.45635537777778,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 210.36254626666664,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 196.30686855333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 152.43323600000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1011.86163206,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1009.75935546,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1030.1032138200003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.037583995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.845861785,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 33.529651566666665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 127.91857272,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 132.06128948,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 116.8807329,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.732514873333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.199214428727274,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.386335136,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 177.8997984,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 93.04410191363635,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 606.3190768000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 235.32340735000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 215.49062952,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 116.4651081111111,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.33158104,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.10682286875,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 69.28419376000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 59.385339810000005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 57.93461294,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.9501521175,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.557953617499999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.416056771428572,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 102.30389834,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 138.06270176,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.65013053999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.423740709999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.768256445000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.397552160000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 142.6415837857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 242.19267968,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 357.4965305333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "1483c2d360a4d377eccff34f35b7b9475d996c63",
          "message": "Release 0.0.7",
          "timestamp": "2024-01-26T10:23:36+01:00",
          "tree_id": "ff3cd3def514323643b687848d15ab7cf9631805",
          "url": "https://github.com/softwaremill/jox/commit/1483c2d360a4d377eccff34f35b7b9475d996c63"
        },
        "date": 1706262039001,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1016.3840819,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 195.94045563333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 115.97908253333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 197.86682282666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 187.22562040666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 149.79586410714285,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1006.57146058,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1007.7216129599999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1024.5407594600001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.836144925000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.91013153,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.928294705000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 127.53673131999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 124.88177838000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 129.61444736,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.768864655555555,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.98259218,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.965836931090909,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 180.6697046333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 87.60620270151514,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 583.1207129000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 236.18699736,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 196.4681982933333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 110.48291954000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.03249902666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.15896224939517,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 71.84346069,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 58.97164435,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 61.150772100000005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.7354468425,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.941652852499999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.061138574285712,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 103.82561278,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 143.30403628,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 95.52575839000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.574559519999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 28.609652434999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.456624855,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 148.41100862857144,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 251.12174964000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 319.70464300000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "934b312eb0cb9c567df6bc5298dfbb1f2b393606",
          "message": "Remove the skip-when-done in selects (#41)",
          "timestamp": "2024-02-07T17:10:39+01:00",
          "tree_id": "91b8d68e5a8b7bcb49b54164e0b272cb227ece74",
          "url": "https://github.com/softwaremill/jox/commit/934b312eb0cb9c567df6bc5298dfbb1f2b393606"
        },
        "date": 1707323242575,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1045.6840648,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 183.03371461904763,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 108.37011247999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 210.74202729999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 178.2099539047619,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 158.46649283333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1011.07744402,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1018.0530355200001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1012.3232074599998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.241173860000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.25988696,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.654498354999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 125.91101185999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 129.80682554,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 124.003375,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.845299237777777,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.980176418000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.493734656363637,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 187.55959506666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 91.78475633181819,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 600.9183256,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 232.30493028,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 197.69057397333336,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 107.26794656000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 53.545331336842104,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 33.557212973333336,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 69.20955209,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 56.07503876,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 61.24125351,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 13.82809196,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.2422397475,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.551668457142856,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 104.6812573,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 140.45718194,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 103.37073859,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 29.302384654999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.173450215000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.995449209999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 140.0468332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 258.41517484999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 337.44749939999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "782ddb93d2d098d8373fc36ec8f8cd7e5f490b57",
          "message": "Implement isClosedForSend / isClosedForReceive (#42)",
          "timestamp": "2024-02-09T14:05:57+01:00",
          "tree_id": "99b124f7f088f2fb2805b007a77af742cba18aba",
          "url": "https://github.com/softwaremill/jox/commit/782ddb93d2d098d8373fc36ec8f8cd7e5f490b57"
        },
        "date": 1707484936025,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1049.7049792,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 218.30436259999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 116.42524282222223,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 215.70474556666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 195.71788710666664,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 157.6859388952381,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 1008.5462200600001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 1024.88434526,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1025.66753338,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.348432064999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 29.6592367,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 32.67436841666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 115.1241248,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 136.92780398,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 128.37055904,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 11.83722471111111,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 10.887091829777777,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.508625722,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 175.59082975714287,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 89.5586707409091,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 638.4829754,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 220.65991881999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 198.4257949133333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 113.36917986666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.315469868095235,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.070966212500004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 71.14426313999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 57.27191369999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 55.91918644,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 14.215238934642858,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 13.755584774999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.301858634285713,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"100\"} )",
            "value": 102.21974118,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"1000\"} )",
            "value": 146.56934434,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 97.56738858,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"100\"} )",
            "value": 30.04304469,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"1000\"} )",
            "value": 31.735491785,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.660835825,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 140.21188145000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 274.11566254999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 347.2484654666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "a5713703b708f699f56f32942021eff2400f51dd",
          "message": "More benchmarks (#43)",
          "timestamp": "2024-02-09T16:36:12+01:00",
          "tree_id": "eed8e5cf440e77b78a0099f8b60f5aa804298eb9",
          "url": "https://github.com/softwaremill/jox/commit/a5713703b708f699f56f32942021eff2400f51dd"
        },
        "date": 1707493725883,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1034.1939179,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 186.75659566666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 116.15575433333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 209.25219756666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 188.41280220000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 147.3767791142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1025.15543436,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.130385589999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 120.19410828,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.008583674727273,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1008.75173498,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 38.25392288666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 109.60712073999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 13.024842255000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 175.21702846666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 88.97492817727272,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 610.0754209,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 220.11142204000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 201.02094920000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 104.34014024000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 49.91738803809524,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.22674180987903,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 58.55864947,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.206550428571429,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 106.08100498000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.95128593,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 70.59701633,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 14.882831300000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 141.26164595,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 269.26181375,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 364.00640899999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "89b6e1f0ff5c1259afb71b5ee8e51db1af97abe2",
          "message": "Remove buffered wrapper, store values directly (#46)",
          "timestamp": "2024-02-12T15:41:02+01:00",
          "tree_id": "c1db42065c9b54b7a4b99efbda4a5824a2d361f6",
          "url": "https://github.com/softwaremill/jox/commit/89b6e1f0ff5c1259afb71b5ee8e51db1af97abe2"
        },
        "date": 1707749626166,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1077.4314248,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 178.86276863333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 117.3822788,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 198.83542306666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 179.2806239,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 145.13898775,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1021.55348652,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.663666654999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 115.78857466,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.837270578181819,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1002.8787861799999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 37.165178833333336,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 129.67741422,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 13.047026752499999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 179.50175646666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 87.84043298776223,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 631.6247,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 205.25311777333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 193.24618949999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 159.98135113333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 49.89598031190476,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 33.52310348666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 60.77118996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.447590085714285,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.07374617,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.260539084999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 89.18860347,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.72539046,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 146.9784189142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 236.89417388,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 353.81547439999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "c96e9b5c6ea500cf54712179fc23611e0970f419",
          "message": "Cleanup send segments when closed, fix segment estimate in test (#47)",
          "timestamp": "2024-02-12T17:10:10+01:00",
          "tree_id": "accb9e452fad9bde3fd8f09c08cd747343a56ec4",
          "url": "https://github.com/softwaremill/jox/commit/c96e9b5c6ea500cf54712179fc23611e0970f419"
        },
        "date": 1707754980501,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1017.9753855000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 214.11229848,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 118.47923444444443,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 191.24208298000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 177.09383121904762,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 147.78444825000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1023.3522526,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.435521745,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 109.17721584,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.88461459272727,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1003.4005293800001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 37.807089106666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 125.72274526000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 12.848811325,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 182.20663406666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 89.71331124848484,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 615.9013192,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 208.34943886666665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 191.87387703333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 107.00467094000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.86280537894737,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.06880118125,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 56.739154989999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.569735854285714,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 93.29436710999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.09863323,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 64.14075957,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 16.898556726190474,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 139.321365175,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 236.43083959999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 375.9367095333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "260d06f895b047b0af531a692cdc4f70e7caa007",
          "message": "Use volatiles & var handles instead of atomics (#45)",
          "timestamp": "2024-02-13T09:42:13+01:00",
          "tree_id": "2b19744a8f2a1490d6b4389b3ea839c5f1c0a0fc",
          "url": "https://github.com/softwaremill/jox/commit/260d06f895b047b0af531a692cdc4f70e7caa007"
        },
        "date": 1707814500108,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1058.6160506,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 193.19651234666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 116.78125504444445,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 193.62308597333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 168.2123154583333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 144.633007675,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1035.7036673,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 29.60041463,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 107.18971144,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.554023247272728,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 998.6528505600002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 35.778603186666665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 104.71904628,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 13.299519615,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 192.2978804,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 93.92945327121213,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 669.434239,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 226.24765544000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 214.70119672,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 114.44950744444445,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 53.47725092631579,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.614096587096775,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 60.97430584999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.677564931428572,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.81062983000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.369566924999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 80.92968521,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.373333634285714,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 141.6950237,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 245.97159675999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 349.92724799999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "87effb2ce8fc9bf97d570c251bad4b8612750f6a",
          "message": "Immediately remove interrupted-sends-only segments, count interrupted receives differently (#49)",
          "timestamp": "2024-02-19T17:01:26+01:00",
          "tree_id": "2d30bed85f2222aacf37bec7b9134665bc81d602",
          "url": "https://github.com/softwaremill/jox/commit/87effb2ce8fc9bf97d570c251bad4b8612750f6a"
        },
        "date": 1708359258621,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"1\"} )",
            "value": 1282.5989526,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"10\"} )",
            "value": 209.69202848,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 112.75441142222223,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 217.96622784000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 199.41546134,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 164.3527725952381,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1024.7337495000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 28.571032655,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 116.49459706,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.849924354545454,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1011.61565154,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 37.0223378,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 129.63415694,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 12.96088934,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 200.11760694,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 89.9281761439394,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 628.7684548999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 227.4574126,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 211.83465224000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 109.84683082000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.58991948421052,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.15272890504033,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 59.51198259,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.11060466857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 98.99588634,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.125364315000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 69.71728954,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 14.980435891428574,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 136.99846989999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 257.7944378,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 364.0193604666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "2c85114d7c50366841772e15878ca4ff57ed045d",
          "message": "Test with buffer size=16",
          "timestamp": "2024-02-19T17:22:44+01:00",
          "tree_id": "c6c7db4eaeacb406054c9a735fe92decc2b96b05",
          "url": "https://github.com/softwaremill/jox/commit/2c85114d7c50366841772e15878ca4ff57ed045d"
        },
        "date": 1708360595599,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 148.8450206285714,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 110.6382466511111,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 183.16783166666664,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 170.20341196190478,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1017.5721317,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 87.61331281999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.43978573,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 122.29656910000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 16.77907508190476,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.929335065636364,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1008.30079372,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 130.64934494000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 35.99270273333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 118.19205646,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 26.816763564999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 14.184961486428572,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 203.30853901999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 91.79831745454547,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 602.3906932,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 233.77264976,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 210.73030363999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 44.515880530434785,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.95812014375,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 56.09641434,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 18.143451106666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.882046071428572,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.05397962,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 37.56915651333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.447005940000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 66.75720507999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 23.549853008000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.861869160000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 143.74421160000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 256.6022075,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 366.47781946666663,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "10f28eaebeddcc7c31e20b45925aa0053e5909a7",
          "message": "Release 0.1.1",
          "timestamp": "2024-02-19T18:01:50+01:00",
          "tree_id": "a8bf8adf9fd506b68fb9c8bf00bc3deda761dc58",
          "url": "https://github.com/softwaremill/jox/commit/10f28eaebeddcc7c31e20b45925aa0053e5909a7"
        },
        "date": 1708362979518,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 154.48793937142858,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 116.91471084444444,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 185.03666503333335,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 171.63259432380954,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1021.9754861600001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 87.12307533,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 29.909142535,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 102.81861185,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 16.548553536666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.8224278,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 996.9115715800001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 130.05770113999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 37.06309500666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 123.9848295,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 29.940909600000005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 13.928968790000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 203.18772221333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 88.98643052948718,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 586.3032403,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 234.43087864999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 211.83353459999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 44.509070139130436,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.29355743931451,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 59.93804322,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 17.67318241666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.202271374285715,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 94.64025821,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 34.87199235333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.72878549,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 86.49827994,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 23.070312332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.675537782857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 145.70165805714285,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 240.47977855999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 358.1369067333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "0560d426e874d9bd510bdaa87b79b636cf4a3f78",
          "message": "Test results",
          "timestamp": "2024-02-19T21:20:56+01:00",
          "tree_id": "8cf88adf1ee642af5dce971dd2c82fba08db88d9",
          "url": "https://github.com/softwaremill/jox/commit/0560d426e874d9bd510bdaa87b79b636cf4a3f78"
        },
        "date": 1708374886441,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 146.9685492857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 118.184664,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 187.15015125333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 175.45358389999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1017.17590822,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 87.84837825000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.70601862,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 115.62076732,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 19.996332447999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 9.386172469090909,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1011.7736034399999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 129.26748038,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 34.52984635333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 124.14926161999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 24.346238399999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 12.9148525475,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 202.13205620666665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 91.56988288333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 597.1434722,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 231.79928896,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 207.23856344,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 42.90405104166666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.3108948625,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 63.049376089999996,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 18.280372970000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 14.630510902857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 105.49430785999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 34.04186641333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.967642480000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 71.95709582999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 23.259890851999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 14.848881505714285,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 145.51886591428573,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 242.05904900000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 315.34141945,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "53a3496870cb7d0442186f1dc2f26079271fd8cd",
          "message": "Test results",
          "timestamp": "2024-02-19T22:40:36+01:00",
          "tree_id": "80f65bc8a5b863d242bc18a1953a1d987aff0f40",
          "url": "https://github.com/softwaremill/jox/commit/53a3496870cb7d0442186f1dc2f26079271fd8cd"
        },
        "date": 1708379655413,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 175.57647236666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 114.08266937777778,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 194.82128806666665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 167.3214300142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1018.1718929800002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 87.51177995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 29.473761895000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 111.2481972,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 16.877228788095238,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.595596429999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 1009.9417156400001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 130.25444653999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 35.952803159999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 129.21773862,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 24.885032380000006,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 13.0288758125,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 203.24313538,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 89.2276640348485,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 619.7641471,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 239.74106048000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 207.06047352000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 44.829504121739134,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.11244649092742,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 59.598370550000006,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 19.02057679,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 16.872373563809525,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 104.45992038,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 34.28213044,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.354375620000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 82.70022282,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 25.054834629,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 16.681358554285715,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 151.8648988,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 271.10619955000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 318.7844731,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "d2e8d8211dcc048c5b5260b4d7070afb300c5504",
          "message": "Go benchmark",
          "timestamp": "2024-02-20T15:30:36+01:00",
          "tree_id": "39ecc870a59bc3966d95ec3c59fc3d861d5c1f28",
          "url": "https://github.com/softwaremill/jox/commit/d2e8d8211dcc048c5b5260b4d7070afb300c5504"
        },
        "date": 1708440241450,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 152.8464355142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 116.49982395555553,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 176.58807365714284,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 176.02922766666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1007.5537572999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 86.40201934000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 28.953624729999994,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 127.39057402,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 17.01814852333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.397729803999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 911.7233054800001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 119.38842116000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 52.425892596666664,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 117.89756433,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 21.414341699999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 9.182947303636363,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 200.14286943333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 88.49272795163171,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 583.7493178000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 239.32881187000004,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 208.45517896,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 45.11114068695652,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.33215431653226,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 60.90702185,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 16.64728907047619,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.108727374285715,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 103.01792889000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 33.71601838,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.823305244999993,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 66.02172655000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 37.563847839999994,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 14.821260974285712,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 142.4119451,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 242.22800558,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 318.53717314999994,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "b355f53cfcabc47ce151cea9896d67cc76284bd8",
          "message": "Docs",
          "timestamp": "2024-02-21T08:51:51+01:00",
          "tree_id": "4818be4b3608aa867a9af56d1ea07f63e735fbd6",
          "url": "https://github.com/softwaremill/jox/commit/b355f53cfcabc47ce151cea9896d67cc76284bd8"
        },
        "date": 1708502721368,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 144.265681225,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 115.13327113333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 181.165836,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 176.86854830000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1010.3914827800002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 87.43384499,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.00906238,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 123.51990097999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 17.15202119161905,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.95084995488889,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 906.963763,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 119.15075418,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 53.738735543333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 115.38922776,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 21.859835143999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 8.527626216666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 191.14071888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 91.45286423030304,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 605.0693087000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 239.34078024000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 220.16006748,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 43.52887082862319,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.50375143225806,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 58.58676928,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 18.30477497333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.46675314,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 99.37760743000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 34.32820642,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.343378684999998,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 68.85369354,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 21.916070196,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.36326595142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 166.54977597619046,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 267.06067205,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 327.4524973,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "9d73fa47bbe0e89fa92cd469e042f1279c706699",
          "message": "Release 0.1.1",
          "timestamp": "2024-02-27T13:44:53+01:00",
          "tree_id": "b534eb098c6b33efbe49e6256b298301f006bee1",
          "url": "https://github.com/softwaremill/jox/commit/9d73fa47bbe0e89fa92cd469e042f1279c706699"
        },
        "date": 1709038725358,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 145.8229525892857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 115.20718922222223,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 88.21213085000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 78.56902043076924,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1025.35335706,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 28.051858355,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 21.056286736,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 115.4979182,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 20.009141422000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.733561722000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 931.9623208999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 35.095485194999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 21.919735363999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 94.78993075999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 23.320032496000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 8.777776313333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 214.31725576,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 89.69887578333332,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 619.3124563,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 229.85586436,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 208.90508568,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 44.819520999999995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.892946106249997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 61.210563050000005,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 17.361100349999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.723097982857144,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 95.55785482000002,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 36.95466392,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 31.51140992,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 69.78198268999999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 21.984209412,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.35325051142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 147.13771037142857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 249.14917116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 330.67512455,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
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
          "id": "f89bf77f6530fdc07de8d8321c896c28cd94b1df",
          "message": "Update benchmark results",
          "timestamp": "2024-02-27T15:04:25+01:00",
          "tree_id": "ff73649f510776497f0188b8281674fbefe15365",
          "url": "https://github.com/softwaremill/jox/commit/f89bf77f6530fdc07de8d8321c896c28cd94b1df"
        },
        "date": 1709043481113,
        "tool": "jmh",
        "benches": [
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"16\"} )",
            "value": 153.7895866857143,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.arrayBlockingQueue ( {\"capacity\":\"100\"} )",
            "value": 115.91642166666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"16\"} )",
            "value": 84.57718226025641,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 74.58144268571428,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 1022.40488802,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 31.269307765,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.channelChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 18.925055306666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 113.21398598,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 17.122882756666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedBenchmark.queueChain ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 10.04526974,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 917.8065579199999,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 31.020331195,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelChannels ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 20.514023298,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 121.25341646,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 23.037418952,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelBenchmark.parallelQueues ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 8.847733411666667,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.channel",
            "value": 202.88551884666668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.exchanger",
            "value": 92.88651092424242,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousBenchmark.synchronousQueue",
            "value": 578.4610584333334,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithSingleClause",
            "value": 220.88012301333333,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectBenchmark.selectWithTwoClauses",
            "value": 216.98685340000003,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"16\"} )",
            "value": 47.95572044935064,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.BufferedKotlinBenchmark.channel_defaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.45029199354839,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 58.09441187,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 17.42689298,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_defaultDispatcher ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 15.443814357142859,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"0\",\"chainLength\":\"10000\"} )",
            "value": 109.65798084000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"16\",\"chainLength\":\"10000\"} )",
            "value": 37.366695846666666,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ChainedKotlinBenchmark.channelChain_eventLoop ( {\"capacity\":\"100\",\"chainLength\":\"10000\"} )",
            "value": 30.976226175,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"0\",\"parallelism\":\"10000\"} )",
            "value": 66.90470352000001,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"16\",\"parallelism\":\"10000\"} )",
            "value": 21.233483808,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.ParallelKotlinBenchmark.parallelChannels_defaultDispatcher ( {\"capacity\":\"100\",\"parallelism\":\"10000\"} )",
            "value": 15.390777891428574,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.RendezvousKotlinBenchmark.channel_defaultDispatcher",
            "value": 145.9280118285714,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithSingleClause_defaultDispatcher",
            "value": 249.09639871999997,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          },
          {
            "name": "com.softwaremill.jox.SelectKotlinBenchmark.selectWithTwoClauses_defaultDispatcher",
            "value": 325.9298833,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 1\nthreads: 1"
          }
        ]
      }
    ]
  }
}