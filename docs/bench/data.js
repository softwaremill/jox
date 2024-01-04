window.BENCHMARK_DATA = {
    "lastUpdate": 1703168204512,
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
            }
        ]
    }
}
