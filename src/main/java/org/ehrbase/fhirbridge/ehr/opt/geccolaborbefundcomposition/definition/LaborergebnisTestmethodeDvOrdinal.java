package org.ehrbase.fhirbridge.ehr.opt.geccolaborbefundcomposition.definition;

import com.nedap.archie.rm.datavalues.quantity.DvOrdinal;
import javax.annotation.processing.Generated;
import org.ehrbase.client.annotations.Entity;
import org.ehrbase.client.annotations.OptionFor;
import org.ehrbase.client.annotations.Path;
import org.ehrbase.client.classgenerator.interfaces.RMEntity;

@Entity
@Generated(
    value = "org.ehrbase.client.classgenerator.ClassGenerator",
    date = "2021-07-05T14:12:01.364415+02:00",
    comments = "https://github.com/ehrbase/openEHR_SDK Version: 1.4.0"
)
@OptionFor("DV_ORDINAL")
public class LaborergebnisTestmethodeDvOrdinal implements RMEntity, LaborergebnisTestmethodeChoice {
  /**
   * Path: Laborbefund/Laborergebnis/Testmethode/Testmethode
   * Description: Die Beschreibung der Methode, mit dem der Test durchgeführt wurde.
   */
  @Path("")
  private DvOrdinal testmethode;

  public void setTestmethode(DvOrdinal testmethode) {
     this.testmethode = testmethode;
  }

  public DvOrdinal getTestmethode() {
     return this.testmethode ;
  }
}
