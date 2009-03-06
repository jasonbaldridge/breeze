package scalanlp.stats.sampling

import math.Numerics._;

import scalanlp.counters._;
import scalanlp.counters.Counters._;
import scalanlp.collection.mutable.ArrayMap;
import scala.collection.mutable.ArrayBuffer;

/**
 * Represents a Polya distribution, a.k.a Dirichlet compound Multinomial distribution
 * see 
 * http://en.wikipedia.org/wiki/Multivariate_Polya_distribution
 */
class Polya[T](prior: DoubleCounter[T]) extends DiscreteDistr[T] {
  private val innerDirichlet = new Dirichlet(prior);
  def draw() = {
    Multinomial(prior).get;
  }
  
  val logNormalizer = -lbeta(prior);
  
  def probabilityOf(x: T):Double = {
    val ctr = IntCounter[T]();
    ctr.incrementCount(x,1);
    probabilityOf(ctr);
  }
  
  def probabilityOf(x: IntCounter[T]) = Math.exp(logProbabilityOf(x));
  def logProbabilityOf(x: IntCounter[T]) = {
    Math.exp(unnormalizedLogProbabilityOf(x) + logNormalizer);
  }
  
  def unnormalizedLogProbabilityOf(x: IntCounter[T]):Double = {
    val ctr = aggregate(x.map(x => (x._1, x._2.toDouble)));
    val adjustForCount = x.values.foldLeft(lgamma(x.total+1))( (acc,v) => acc-lgamma(v+1))
    ctr += prior;
    adjustForCount + lbeta(ctr);
  }

}


object Polya {
  /**
  * Creates a new symmetric Polya of dimension k
  */
  def sym(alpha : Double, k : Int) = this(Array.fromFunction{ x => alpha }(k));
  
  /**
  * Creates a new Polya of dimension k with the given parameters
  */
  def apply(arr: Array[Double]):Polya[Int] = new Polya[Int](new ArrayMap(new ArrayBuffer[Double] ++ arr) with DoubleCounter[Int])
}