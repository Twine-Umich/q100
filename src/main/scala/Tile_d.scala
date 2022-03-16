// package q100

// import chisel3._
// import chisel3.util._
// import chisel3.twine._
// import chisel3.twine.util._
// import scala.collection.mutable.ArrayBuffer

// class TileD extends TwineModule with Q100Params{

//     // Declare the top layer inputs. Don't Change
//     val in = IO(Input(new TileInput(4)))
//     var out = IO(Output(new TileInput(2)))
//     val ctrl = IO(new DecoupledIOCtrl(3,3))
    
//     // First declare modules
//     val colSelect0 = Module(new ColSelect(4, 0)) // This gives us col 0
//     val colSelect1 = Module(new ColSelect(4, 1)) // This gives us col 1
//     val colSelect2 = Module(new ColSelect(4, 2)) // This gives us col 2

//     // This takes input and compares it to 6. True if the result is greater than 6
//     val boolGen = Module(new BoolGen(BoolGenOP.GT, 6.U)) 

//     // These modules filter out row based on the boolgen results
//     val colFilter0 = Module(new ColFilter)
//     val colFilter1 = Module(new ColFilter)
//     val colFilter2 = Module(new ColFilter)

//     // Add the two inputs together
//     val alu = Module(new ALU(AluOp.ADD))

//     // The aggregatpr
//     val aggregate = Module(new Aggregator)

//     // Connect inputs to colselects
//     in >>> colSelect0
//     in >>> colSelect1
//     in >>> colSelect2

//     // Filter out the rows where the value in column 0 is greater than 6 or (Col 0 > 6)
//     colSelect0 >>> boolGen

//     boolGen >>> colFilter0
//     TwineBundle(colSelect1.out.dat, boolGen.out.cmp, colSelect1.out.EOF) >>> colFilter1
//     TwineBundle(colSelect2.out.dat, boolGen.out.cmp, colSelect2.out.EOF) >>> colFilter2

//     // 3. Add column 1 and column 2 together (col1 + col2)
//     TwineBundle(colFilter1.out.dat, colFilter2.out.dat, colFilter1.out.EOF) >>> alu

//     // 4. Aggregate the results in step 3 by col 0.
//     TwineBundle(colFilter0.out.dat, alu.out.dat, alu.out.EOF) >>> aggregate

//     aggregate >>> out
// }