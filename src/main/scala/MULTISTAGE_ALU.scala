package q100
import chisel3._
import chisel3.util._
import chisel3.twine._
import chisel3.twine.util._

class MULTISTAGE_ALUInput(val veclen: Int)  extends Bundle with Q100Params{
		val reference = Vec(veclen, UInt(XLEN.W))
    val a = Vec(veclen, UInt(XLEN.W))
    val b = Vec(veclen, UInt(XLEN.W))
    val EOF = Vec(veclen, Bool()) // This would indicate the end of whole column. It would be one cycle behind the valid row
}
class MULTISTAGE_ALUOutput(val veclen: Int)  extends Bundle with Q100Params{
		val reference = Vec(veclen, UInt(XLEN.W))
    val dat = Vec(veclen, UInt(XLEN.W))
    val EOF = Vec(veclen, Bool())// This would indicate the end of whole column. It would be one cycle behind the valid row
}
import AluOp._
class MULTISTAGE_ALU(val veclen: Int) extends TwineModule with Q100Params{
    val in = IO(Input(new MULTISTAGE_ALUInput(veclen)))
    val out = IO(Output(new MULTISTAGE_ALUOutput(veclen)))
    val ctrl = IO(new DecoupledIOCtrl(2,2))
		val go = RegInit(true.B)
    val num_of_cycles = 6
    val v = ctrl.in.valid && ctrl.in.ready
    ctrl.in.ready := ctrl.out.ready & go
		val result1 = Wire(UInt(XLEN.W))
		(in.a(0) + in.b(0)) >>> result1
		val pip_ref1 = Pipe(v, in.reference(0), num_of_cycles)
		val pip_result1 = Pipe(v, result1, num_of_cycles)
		val pip_eof1 = Pipe(v, in.EOF(0), num_of_cycles)
		out.dat(0) := pip_result1.bits
		out.EOF(0) := pip_eof1.bits
		out.reference(0) := pip_ref1.bits
		for(i <- 1 until veclen){
    	val result = Wire(UInt(XLEN.W))
			(in.a(i) + in.b(i)) >>> result
			val pip_ref = Pipe(v, in.reference(i), num_of_cycles)
			val pip_result = Pipe(v, result, num_of_cycles)
			val pip_eof = Pipe(v, in.EOF(i), num_of_cycles)
			out.dat(i) := pip_result.bits
    	out.EOF(i) := pip_eof.bits
			out.reference(i) := pip_ref.bits
		}
		when(v){
			go := false.B
		}
		when(ctrl.out.valid){
			go := true.B
		}
    ctrl.out.valid := pip_result1.valid

}