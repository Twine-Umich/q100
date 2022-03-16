package q100

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl._
import chisel3.twine._
import chisel3.twine.util._
import chisel3.internal._ 

class ColSelectInput(val num_of_col:Int) extends Bundle with Q100Params{
    val dat = Vec(num_of_col,UInt(XLEN.W))
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class ColSelectOutput extends Bundle with Q100Params{
    val dat = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class ColSelect(val width:Int, val sel: Int) extends TwineModule with Q100Params{
    val in = IO(Input(new ColSelectInput(width)))
    val out = IO(Output(new ColSelectOutput))
    val ctrl = IO(new DecoupledIOCtrl(2,2))
    in.EOF >>> out.EOF
    in.dat(sel) >>> out.dat

    ctrl.out.ready >>> ctrl.in.ready
    ctrl.in.valid >>> ctrl.out.valid
}