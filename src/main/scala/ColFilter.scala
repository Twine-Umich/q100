package q100

import chisel3._
import chisel3.util._
import chisel3.twine._
import chisel3.twine.util._

class ColFilterInput extends Bundle with Q100Params{
    val dat = UInt(XLEN.W)
    val cmp = Bool()
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class ColFilterOutput extends Bundle with Q100Params{
    val dat = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class ColFilter extends TwineModule with Q100Params{
    val in = IO(Input(new ColFilterInput))
    val out = IO(Output(new ColFilterOutput))
    val ctrl = IO(new DecoupledIOCtrl(2,2))

    in.EOF >>> out.EOF
    in.dat >>> out.dat
    ctrl.out.ready >>> ctrl.in.ready

    Mux(in.cmp || in.EOF, ctrl.in.valid, false.B) >>> ctrl.out.valid
}