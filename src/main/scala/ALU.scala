package q100

import chisel3._
import chisel3.util._
import chisel3.simplechisel._
import chisel3.simplechisel.util._

object AluOp extends Enumeration {
  type AluOp = Value
  val ADD, MINUS, GT, GET, LT, LET, EQ, NEQ = Value
}

class ALUInput extends Bundle with Q100Params{
    val a = UInt(XLEN.W)
    val b = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class ALUOutput extends Bundle with Q100Params{
    val dat = UInt(XLEN.W)
    val EOF = Bool() // This would indicate the end of whole column. It would be one cycle behind the valid row
}
import AluOp._
class ALU(val opType: AluOp) extends SimpleChiselModule with Q100Params{
    val in = IO(Input(new ALUInput))
    val out = IO(Output(new ALUOutput))
    val ctrl = IO(new DecoupledIOCtrl(4,4))

    ctrl.out.ready >>> ctrl.in.ready
    ctrl.in.valid >>> ctrl.out.valid
    in.EOF >>> out.EOF
    opType match{
        case AluOp.ADD => (in.a + in.b) >>> out.dat
        case AluOp.MINUS => (in.a - in.b) >>> out.dat 
        case AluOp.GT => (in.a > in.b) >>> out.dat 
        case AluOp.GET => (in.a >= in.b) >>> out.dat 
        case AluOp.LT => (in.a < in.b) >>> out.dat
        case AluOp.LET => (in.a <= in.b) >>> out.dat 
        case AluOp.EQ => (in.a === in.b) >>> out.dat 
        case AluOp.NEQ => (in.a =/= in.b) >>> out.dat 
    }
}