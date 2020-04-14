resolvers in ThisBuild += "lightbend-commercial-mvn" at
  "https://repo.lightbend.com/pass/DKKmZaxU6F22sMCgmfEhaYPGvTxOZO6Y0VHUqPYdvz7Wt_A5/commercial-releases"
resolvers in ThisBuild += Resolver.url("lightbend-commercial-ivy",
  url("https://repo.lightbend.com/pass/DKKmZaxU6F22sMCgmfEhaYPGvTxOZO6Y0VHUqPYdvz7Wt_A5/commercial-releases"))(Resolver.ivyStylePatterns)