package eu.fakod.sjersey.inject

import javax.ws.rs.QueryParam
import org.glassfish.jersey.server.model.Parameter
import org.glassfish.jersey.server.internal.inject._
import org.glassfish.jersey.server.ParamException
import org.glassfish.hk2.api.{InjectionResolver, TypeLiteral, Factory, ServiceLocator}
import eu.fakod.sjersey.inject.ScalaCollectionsQueryParamFactoryProvider.QueryParamValueFactory
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider
import javax.inject.Singleton
import javax.ws.rs.ext.Provider


//@Provider
//class ScalaCollectionsQueryParamInjectableProvider extends InjectableProvider[QueryParam, Parameter] {
//  def getScope = ComponentScope.PerRequest
//  def getInjectable(ic: ComponentContext, a: QueryParam, c: Parameter): Injectable[_] = {
//    val parameterName = c.getSourceName()
//    if (parameterName != null && !parameterName.isEmpty) {
//      buildInjectable(parameterName, c.getDefaultValue, !c.isEncoded, c.getParameterClass)
//    } else null
//  }
//
//  private def buildExtractor(name: String, default: String, klass: Class[_]): MultivaluedParameterExtractor = {
//    if (klass == classOf[Seq[String]]) {
//      new ScalaCollectionStringReaderExtractor[Seq](name, default, Seq)
//    } else if (klass == classOf[List[String]]) {
//      new ScalaCollectionStringReaderExtractor[List](name, default, List)
//    } else if (klass == classOf[Vector[String]]) {
//      new ScalaCollectionStringReaderExtractor[Vector](name, default, Vector)
//    } else if (klass == classOf[IndexedSeq[String]]) {
//      new ScalaCollectionStringReaderExtractor[IndexedSeq](name, default, IndexedSeq)
//    } else if (klass == classOf[Set[String]]) {
//      new ScalaCollectionStringReaderExtractor[Set](name, default, Set)
//    } else if (klass == classOf[Option[String]]) {
//      new ScalaOptionStringExtractor(name, default)
//    } else null
//  }
//
//  private def buildInjectable(name: String, default: String, decode: Boolean, klass: Class[_]): Injectable[_ <: Object] = {
//    val extractor = buildExtractor(name, default, klass)
//    if (extractor != null) {
//      new ScalaCollectionQueryParamInjectable(extractor, decode)
//    } else null
//  }
//}

object ScalaCollectionsQueryParamFactoryProvider {

  class InjectionResolver extends ParamInjectionResolver[QueryParam](classOf[ScalaCollectionsQueryParamFactoryProvider]) {}

  class QueryParamValueFactory(extractor: MultivaluedParameterExtractor[_], decode: Boolean) extends AbstractContainerRequestValueFactory[AnyRef] {

    def provide: AnyRef = try {
      extractor.extract(getContainerRequest.getUriInfo.getQueryParameters(decode)).asInstanceOf[AnyRef]
    } catch {
      case e: ExtractorException =>
        throw new ParamException.QueryParamException(e.getCause, extractor.getName, extractor.getDefaultValueString)
    }
  }

}

class ScalaCollectionsQueryParamFactoryProvider(mpep: MultivaluedParameterExtractorProvider, locator: ServiceLocator) extends AbstractValueFactoryProvider(mpep, locator, Parameter.Source.QUERY) {

  def createValueFactory(parameter: Parameter): AbstractContainerRequestValueFactory[_] = {
    val parameterName = parameter.getSourceName
    if (parameterName == null || parameterName.length() == 0) {
      return null
    }

    //val e = get(parameter)
    val e = buildExtractor(parameterName, parameter.getDefaultValue, parameter.getRawType)
    if (e == null) {
      return null
    }

    new QueryParamValueFactory(e, !parameter.isEncoded)
  }

  private def buildExtractor(name: String, default: String, klass: Class[_]): MultivaluedParameterExtractor[_] = {
    if (klass == classOf[Seq[String]]) {
      new ScalaCollectionStringReaderExtractor[Seq](name, default, Seq)
    } else if (klass == classOf[List[String]]) {
      new ScalaCollectionStringReaderExtractor[List](name, default, List)
    } else if (klass == classOf[Vector[String]]) {
      new ScalaCollectionStringReaderExtractor[Vector](name, default, Vector)
    } else if (klass == classOf[IndexedSeq[String]]) {
      new ScalaCollectionStringReaderExtractor[IndexedSeq](name, default, IndexedSeq)
    } else if (klass == classOf[Set[String]]) {
      new ScalaCollectionStringReaderExtractor[Set](name, default, Set)
    } else if (klass == classOf[Option[String]]) {
      new ScalaOptionStringExtractor(name, default)
    } else null
  }
}


class ParameterInjectionBinder extends AbstractBinder {
  def configure(): Unit = {
    bind(classOf[ScalaCollectionsQueryParamFactoryProvider]).to(classOf[ValueFactoryProvider]).in(classOf[Singleton])

    bind(classOf[ScalaCollectionsQueryParamFactoryProvider.InjectionResolver]).
      to(new TypeLiteral[InjectionResolver[QueryParam]]() {
    }).in(classOf[Singleton])
  }
}