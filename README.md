# PnL Calculator

## Objective:
Given a CSV with simplified trade data, calculate and display PnL in USD

## Features:
- Application should read trade booking info from a CSV file
- Calculate the PnL in USD aggregated based on:
  - BBGCode
  - org.example.engine.Portfolio
  - org.example.engine.Strategy
  - org.example.engine.User
  - org.example.shared.Currency

# Things to keep in mind:
- The same BBGCode may occur in different currencies. Convert all amounts to USD using static conversion rates
- Must handle new trades, cancellations and amends
  - New trades: havent appeared before
  - Amends: update to previous trades (matching trade IDs). Only include the most recent amend
  - Cancellations: removal of a trade (new or amended). Exclude cancelled trade from PnL calculation
- When calculating PnL, account for the side of the trade (buy or sell)
- Interactive interface: Create a CLI to manually add new trades and cancel existing ones, in addition to those loaded
  from the CSV file

# Next Steps:
- Instead of everything being in the same process, separate the CSV reader into a separate process, the CLI into its 
  own process and the engine into a different process (separate containers). They can communicate using ring buffers
  between them. This means the engine can run independently indexing all trades while the CSV reader can take as long
  as it wants to send everything over. Can have the CSV reader send each trade over the ring buffer one by one
- Once ring buffers are used for communication between components, can use Aeron instead of a shared memory ring buffer
  so the components can be running on separate machines communicating over UDP. This means we can turn on as many of
  the readers/clis as we want (can also change out the readers/clis for other "gateways" that can send trades over 
  to be indexed)

# How to run:
I saved an intellij run configuration to the .idea folder in the project. If you load the project, you can run the
program where it will read in the CSV file from the reader modules production resources folder. If you would like to 
choose a different file to run with, you can edit the run config and point the runner to a different csv file.