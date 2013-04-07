import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import gnu.io.CommPortIdentifier
import gnu.io.SerialPort
import gnu.io.SerialPortEvent
import gnu.io.SerialPortEventListener
import java.util.Enumeration
import scala.util.control.Breaks._

class SerialTestScala extends SerialPortEventListener {
  import SerialTestScala._

  def initialize {
    System.load(new File("/Users/chris/dev/quark/librxtxSerial.jnilib").getAbsolutePath)
    var portId: CommPortIdentifier = null
    val portEnum: Enumeration[_] = CommPortIdentifier.getPortIdentifiers

    breakable {
      while (portEnum.hasMoreElements) {
        val currPortId: CommPortIdentifier = portEnum.nextElement.asInstanceOf[CommPortIdentifier]
        for (portName <- PORT_NAMES) {
          if (currPortId.getName == portName) {
            portId = currPortId
            break //todo: break is not supported
          }
        }
      }
    }

    if (portId == null) {
      System.out.println("Could not find COM port.")
      return
    }
    try {
      serialPort = portId.open(this.getClass.getName, TIME_OUT).asInstanceOf[SerialPort]
      serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
      input = new BufferedReader(new InputStreamReader(serialPort.getInputStream))
      output = serialPort.getOutputStream
      serialPort.addEventListener(this)
      serialPort.notifyOnDataAvailable(true)
    }
    catch {
      case e: Exception => {
        System.err.println(e.toString)
      }
    }
  }

  /**
   * This should be called when you stop using the port.
   * This will prevent port locking on platforms like Linux.
   */
  def close {
    if (serialPort != null) {
      serialPort.removeEventListener
      serialPort.close
    }
  }

  /**
   * Handle an event on the serial port. Read the data and print it.
   */
  def serialEvent(oEvent: SerialPortEvent) {
    if (oEvent.getEventType == SerialPortEvent.DATA_AVAILABLE) {
      try {
        val inputLine: String = input.readLine
        System.out.println(inputLine)
      }
      catch {
        case e: Exception => {
          System.err.println(e.toString)
        }
      }
    }
  }

  var serialPort: SerialPort = null
  /**
   * A BufferedReader which will be fed by a InputStreamReader
   * converting the bytes into characters
   * making the displayed results codepage independent
   */
  private var input: BufferedReader = null
  /** The output stream to the port */
  private var output: OutputStream = null
}

object SerialTestScala {
  def main(args: Array[String]) {
    val main: SerialTest = new SerialTest
    main.initialize
    val t: Thread = new Thread {
      override def run {
        try {
          Thread.sleep(1000000)
        }
        catch {
          case ie: InterruptedException => {
          }
        }
      }
    }
    t.start
    System.out.println("Started")
  }

  /** The port we're normally going to use. */
  private final val PORT_NAMES = Array("/dev/tty.usbserial-A9007UX1", "/dev/ttyUSB0", "COM3")
  /** Milliseconds to block while waiting for port open */
  private final val TIME_OUT: Int = 2000
  /** Default bits per second for COM port. */
  private final val DATA_RATE: Int = 9600
}