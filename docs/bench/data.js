window.BENCHMARK_DATA = {
  "lastUpdate": 1703016150325,
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1167.001781656675,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 242.33029264051797,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 146.32382387748888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 200.74571751295636,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 154.71987941923126,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 128.89720161954193,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 195.89987459151513,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 142.57188373345238,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 123.66945064025053,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 187.04570474424654,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 163.78871707136753,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 97.50455660042537,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1167.9277664850035,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 218.62314537793637,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 140.21800576099184,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 202.0358698598211,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 137.2828064957984,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 121.564440399437,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 198.12834607555558,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 143.11023811146825,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 116.05623691542011,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 194.1301427432577,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 167.20325645897438,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 90.47188968619234,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 204.9856305476206,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 148.06291144961654,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.48136803822822,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.60889948340701,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1168.4591950078354,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 222.52599611710087,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 145.37992291545532,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 223.28946717602614,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 152.0510172852984,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 119.23137626064262,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 205.20663572,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 153.88474577619047,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 125.09345702821068,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 189.44188332132,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 165.59084724766902,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 96.7988260696449,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 196.44564897054175,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.00676541130606,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.31508666193937,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.739077184928338,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1145.9882633027214,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 230.05143043783164,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 148.9199744647137,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 216.30054494251894,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 181.60706853639925,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 157.71955688000668,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 198.88669172888888,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 173.62090278667443,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 157.235613301917,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 183.72219768571466,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 175.53289324188034,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 100.61646365552605,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 203.00967853933665,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.64800416268777,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.15184426471544,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 31.999848296296292,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1200.94677019116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 223.64445438782178,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 153.02887834672867,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 221.81299121065607,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 185.91034854088582,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 148.2467677292731,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 208.17019090757577,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 189.25225266427347,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 147.07828495924298,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 194.07197075459206,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 184.98629408484845,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 97.63218227193512,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 203.2516165794247,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 107.92735409649121,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 51.89057783396537,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 33.80565483514905,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1252.7958927439597,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 247.90211371363213,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 147.91108607108896,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 207.30107078500902,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 180.56537597031522,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 156.92041962026488,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 202.96635070278168,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 169.94600597393165,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 152.5142713597222,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 198.3914138332995,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 182.14265280101012,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 93.65679434041186,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 196.89753048234982,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 114.3989766828116,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 50.17820110979154,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.701432189153444,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
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
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"1\"} )",
            "value": 1204.103555436857,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"10\"} )",
            "value": 222.64249648776416,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.array_blocking_queue ( {\"capacity\":\"100\"} )",
            "value": 152.0082476178817,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"1\"} )",
            "value": 209.0494540081219,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"10\"} )",
            "value": 192.37416379485663,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel ( {\"capacity\":\"100\"} )",
            "value": 145.34176870676018,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"1\"} )",
            "value": 211.89597629468014,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"10\"} )",
            "value": 182.604434939899,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedBenchmark.channel_iterative ( {\"capacity\":\"100\"} )",
            "value": 144.7982774478045,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.channel",
            "value": 199.8031650029824,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.channel_iterative",
            "value": 192.92166361858588,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousBenchmark.exchanger",
            "value": 94.58638186494366,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.RendezvousBenchmark.synchronous_queue",
            "value": 199.56475317811257,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 2"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"1\"} )",
            "value": 112.79008282962964,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"10\"} )",
            "value": 52.49524551773574,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.BufferedKotlinBenchmark.sendReceiveUsingDefaultDispatcher ( {\"capacity\":\"100\"} )",
            "value": 32.80819852292712,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          },
          {
            "name": "jox.RendezvousKotlinBenchmark.sendReceiveUsingDefaultDispatcher",
            "value": 144.14776472253968,
            "unit": "ns/op",
            "extra": "iterations: 5\nforks: 3\nthreads: 1"
          }
        ]
      }
    ]
  }
}