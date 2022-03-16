package q100

import chisel3._
import chisel3.util._
import chisel3.twine._
import chisel3.twine.util._


object BoolGenOP extends Enumeration {
  type BoolGenOP = Value
  val GT, GET, LT, LET, EQ, NEQ = Value
}

class BoolGenInput extends Bundle with Q100Params{
    val dat = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class BoolGenOutput extends Bundle with Q100Params{
    val dat = UInt(XLEN.W)
    val cmp = Bool()
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}

import BoolGenOP._
class BoolGen(val boolGenOp:BoolGenOP, val cmpRef:UInt) extends TwineModule with Q100Params{
    val in = IO(Input(new BoolGenInput))
    val out = IO(Output(new BoolGenOutput))
    val ctrl = IO(new DecoupledIOCtrl(4,4))

    in.EOF >>> out.EOF
    in.dat >>> out.dat
    ctrl.out.ready >>> ctrl.in.ready
    ctrl.in.valid >>> ctrl.out.valid

    boolGenOp match{
        case BoolGenOP.GT => (in.dat > cmpRef) >>> out.cmp 
        case BoolGenOP.GET => (in.dat >= cmpRef) >>> out.cmp 
        case BoolGenOP.LT => (in.dat < cmpRef) >>> out.cmp
        case BoolGenOP.LET => (in.dat <= cmpRef) >>> out.cmp 
        case BoolGenOP.EQ => (in.dat === cmpRef) >>> out.cmp 
        case BoolGenOP.NEQ => (in.dat =/= cmpRef) >>> out.cmp 
    }
}