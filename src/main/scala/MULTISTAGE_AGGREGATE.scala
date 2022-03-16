// package q100

// import chisel3._
// import chisel3.util._
// import chisel3.twine._
// import chisel3.twine.util._

// class MULTISTAGE_AGGREGATE_A_IO(vec:Int) extends Bundle with Q100Params{
//     val reference = Vec(vec,UInt(XLEN.W))
//     val dat = Vec(vec,UInt(XLEN.W))
//     val EOF = Vec(vec,Bool()) // This would indicate the end of whole column. It would be one cycle behind the valid row
// }

// class MULTISTAGE_AGGREGATE_A(vec:Int) extends TwineModule with Q100Params{
//     val in = IO(Input(new MULTISTAGE_AGGREGATE_A_IO(vec)))
//     val out = IO(Output(new MULTISTAGE_AGGREGATE_A_IO(vec)))
//     val ctrl = IO(new DecoupledIOCtrl(2,2))

// 		for(i <- 0 until vec){
// 			val aggregate = Module(new Aggregator)
// 		}
// }