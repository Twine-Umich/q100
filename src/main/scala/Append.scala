package q100

import chisel3._
import chisel3.util._
import chisel3.twine._
import chisel3.twine.util._

class AppendInput(val num_col_a:Int, val num_col_b:Int) extends Bundle with Q100Params{
    val a = Vec(num_col_a, UInt(XLEN.W))
    val b = Vec(num_col_b, UInt(XLEN.W))
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class AppendOutput(val num_col_a:Int, val num_col_b:Int) extends Bundle with Q100Params{
    val dat = Vec(num_col_a + num_col_b, UInt(XLEN.W))
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class Append(val num_col_a:Int, val num_col_b:Int) extends TwineModule with Q100Params{
    val in = IO(Input(new AppendInput(num_col_a, num_col_b)))
    val out = IO(Output(new AppendOutput(num_col_a, num_col_b)))
    val ctrl = IO(new DecoupledIOCtrl(4,4))

    ctrl.out.ready >>> ctrl.in.ready
    ctrl.in.valid >>> ctrl.out.valid
    TwineBundle(in.a, in.b, in.EOF) >>> out
}