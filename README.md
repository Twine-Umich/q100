# Q100 in Twine

## Menu

- [Overview](#overview)
- [File Structure](#file-structure)
- [Testing](#testing)
- [Components](#components)
- [Configuration](#configuration)
- [Synth](#synth)

## Overview

Q100 in Twine is a design based on the [Q100](https://dl.acm.org/doi/10.1145/2644865.2541961) paper. This repo composes of multiple components, a testing framework built with Verilator, and a synthesis configuration built with Yosys.

The basic timing assumptions of this design is that a transaction happens when the valid and ready are `hi` in the same cycle. Whenever a stream is completed, set the EOF signal to hi for one cycle to indicate the stream is over.
As an example, assume we want to compute a data stream composed of data 1, 2, 3.

```text
signal | cycle 1 | cycle 2 | cycle 3 | cycle 4 | cycle 5
--------------------------------------------------------
valid  | hi      | hi      | hi      | hi      | lo
in     | 1       | 2       | 3       | DONTCARE|DONTCARE
EOF    | lo      | lo      | lo      | hi      | lo
```

## File Structure

```script
- generated-src: the generated firrtl design and verilog design
- input_files: Samples test inputs
- obj_dir: the compiled c simulation program headers and source files
- output_files： This is the default destination for output from the verilator
- src/main/scala: scala source code
- tests: contains configuration file and verilator program template
- Makefile
- synth.ys
- vsclib013.lib
```

## Running

To run verilator, run:

```script
make run-verilator
```

## Components

### Aggragator

This module aggregates the data based on the reference 

```scala
class AggregatorIO extends Bundle with Q100Params{
    val reference = UInt(XLEN.W)
    val dat = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
```

For example, if we have a table look at this

```text
user_id payment
1	2
1	5
2	5
2	6
3	1
```

If we use `user_id` as `reference`, and `payment` as `dat`. The result would be

```text
1	7
2	11
3	1
```

### ALU

ALU takes in two data `a` and `b`, and compute the result based on the operator configured through argument.

```scala
// An example of creating a alu
val alu = Module(new ALU(ALUOp.ADD))
```

The operators supported by ALU are

```scala
ADD, MINUS, GT, GET, LT, LET, EQ, NEQ
```

### Append

Append appends two tables into one. It takes two arguments: the number of columns of the first table, the number of columns of the second table

```scala
// An example of creating an append
val append = Module(new Append(2,3))
```

### BoolGen

BoolGen compares the `dat` to the reference based on the operator passed in as an argument. The output is the result of the comparing.

```scala
// An example of creating an boolGen ( whether the data is greater than 3)
val boolGen = Module(new BoolGen(BoolGenOP.GT,3.U))
```

Supported operators are

```scala
GT, GET, LT, LET, EQ, NEQ
```

### Colfilter

After BoolGen stage, you would need to filter out the rows that you interested in. It takes in two data flows: the data to be filtered and the bool value to make the decision.

### ColSelect

ColSelect to filter one of the columns from the input table.

```scala
// An example of creating an colselect ( the 0th column from a table of 5 columns)
val colSelect = Module(new ColSelect(5,0))
```

## Configuration

To help test based on the configuration and topology you created, you need to edit the confiuration file under `tests/config.json`.

## Synth

To synthesize the design with Yosys, run:

```script
make synth
```
