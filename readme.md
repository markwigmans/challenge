# Ximedes SVA Challenge

# integration tests

- gatling:execute -Dgatling.simulationClass=sva.InitSimulation
- gatling:execute -Dgatling.simulationClass=sva.LoadSimulation

docker run -d -p 80:80 -p 8125:8125/udp -p 8126:8126 -p 81:81  kamon/grafana_graphite