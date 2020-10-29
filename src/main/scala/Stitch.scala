package q100

import chisel3._
import chisel3.util._
import chisel3.simplechisel._
import chisel3.simplechisel.util._

class StitchInput extends Bundle with Q100Params{
    val A = UInt(XLEN.W)
    val B = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class StitchOutput extends Bundle with Q100Params{
    val dat = Vec(2, UInt(XLEN.W))
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class Stitch extends SimpleChiselModule with Q100Params{
    val in = IO(Input(new StitchInput))
    val out = IO(Output(new StitchOutput))
    val ctrl = IO(new DecoupledIOCtrl(4,4))

    ctrl.out.ready >>> ctrl.in.ready
    ctrl.in.valid >>> ctrl.out.valid
    SimpleChiselBundle(in.A, in.B, in.EOF) >>> out
}