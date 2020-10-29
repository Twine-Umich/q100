// See LICENSE for license details.

package q100

import java.io.{File, FileWriter}

object Main extends App {
  val dir = new File(args(0)) ; dir.mkdirs
  
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new Tile))
  val writer = new FileWriter(new File(dir, s"${chirrtl.main}.fir"))
  writer write chirrtl.serialize
  writer.close

  val verilog = new FileWriter(new File(dir, s"${chirrtl.main}.v"))
  val verilog_str = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm)).getEmittedCircuit.value
  verilog write verilog_str
  verilog.close
  
}
