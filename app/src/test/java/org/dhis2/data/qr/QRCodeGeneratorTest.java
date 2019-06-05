package org.dhis2.data.qr;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class QRCodeGeneratorTest {

    String veryLongLorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer arcu nulla, pretium ac dictum vel, euismod at massa. Duis euismod malesuada massa at placerat. Quisque accumsan tellus justo, eget rhoncus odio pellentesque ultrices. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus elementum lobortis sem. Nunc tincidunt dui accumsan mauris egestas ultrices. Etiam velit risus, accumsan eu dolor nec, varius tincidunt elit. Nullam porta malesuada urna, in tempus lacus eleifend et. Praesent ac diam odio. Mauris porttitor elit quis lorem pulvinar, eget porttitor urna cursus. In a consectetur lorem, nec hendrerit velit. Nam blandit vel eros ut malesuada. Suspendisse massa nibh, bibendum consectetur nulla vel, iaculis scelerisque enim. Etiam non justo in eros blandit laoreet id vel magna. Morbi et enim eget arcu volutpat aliquam et eu leo. Nam ac odio dapibus, bibendum leo at, euismod dui.\n" +
            "\n" +
            "Cras turpis nisi, molestie sed lectus et, vestibulum faucibus leo. Vestibulum et lectus ut libero ornare suscipit tempus posuere quam. Suspendisse egestas ipsum nisi, vitae tincidunt urna placerat eget. Nulla hendrerit posuere lorem sit amet aliquam. Sed varius viverra nunc, in varius nisi tempus sed. Donec ut auctor tortor. Maecenas id lacus velit. Suspendisse nec vestibulum lacus. Nunc arcu ligula, pellentesque vel tempor sed, fringilla a lacus. Aenean scelerisque cursus elit et eleifend. Phasellus felis sapien, aliquam ac varius ullamcorper, lobortis a nisl.\n" +
            "\n" +
            "Etiam non mauris et lectus volutpat pellentesque eget non urna. Aliquam eget lacus nec ex accumsan elementum. Mauris rutrum dapibus aliquam. Sed arcu diam, pulvinar ut sem nec, semper elementum massa. Phasellus quis commodo massa. Vivamus semper ipsum in orci imperdiet vehicula. Morbi aliquet ex diam, quis pretium erat aliquet mattis. Quisque sollicitudin mi in justo dapibus pharetra. Fusce pharetra, urna nec gravida tempus, magna massa pulvinar sapien, vel aliquet ante augue sit amet nibh. Sed in congue libero, sed vulputate nisl. In quis suscipit eros. Curabitur commodo interdum elit a feugiat. Aenean fringilla elit ac euismod consectetur. Nunc nec luctus nunc.\n" +
            "\n" +
            "Aenean aliquet, sapien sodales blandit eleifend, ipsum purus pellentesque massa, id lacinia lectus libero at purus. Suspendisse sagittis a erat nec tristique. Aliquam ultrices interdum congue. Maecenas placerat justo nisl, et commodo tortor mattis efficitur. Phasellus imperdiet diam eget mattis aliquam. Curabitur egestas, augue quis interdum elementum, leo odio sagittis augue, ac pharetra nisl lectus sed metus. Cras rutrum turpis id metus lacinia elementum. Sed vitae scelerisque dui, congue vestibulum enim. Suspendisse potenti. Praesent ut pretium enim. Maecenas tempor mauris ut gravida lacinia. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Etiam vulputate ut magna sit amet congue. Donec at erat non velit suscipit ornare. Maecenas lacinia varius purus, vitae euismod leo feugiat sed.\n" +
            "\n" +
            "Nunc in nunc neque. Duis convallis pulvinar tortor vel tempus. Etiam velit felis, malesuada sit amet dignissim in, porta a augue. Vestibulum vitae cursus nulla, vitae euismod turpis. Donec vel nisi vitae dolor blandit efficitur. Quisque magna felis, sollicitudin vel ante ac, consectetur feugiat nulla. Quisque ullamcorper enim at arcu iaculis, ac pretium lacus tristique. Mauris varius non orci non dignissim. Nam malesuada arcu nisl. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse viverra pretium elit. Morbi vel finibus erat, nec accumsan nibh. Interdum et malesuada fames ac ante ipsum primis in faucibus.\n" +
            "\n" +
            "Sed ut volutpat justo, sagittis gravida urna. Praesent porta venenatis quam, id scelerisque ante porttitor non. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nulla ullamcorper in lacus at dictum. Morbi imperdiet risus lorem, volutpat maximus mauris congue non. Vestibulum maximus sapien vitae arcu cursus, quis rhoncus nibh tincidunt. Ut malesuada orci quis nisi cursus, non lobortis ante sollicitudin. Aenean lacus massa, congue sed commodo ut, feugiat in lectus. Nullam non nunc ut orci finibus porta non a urna. Nunc sit amet urna sed tellus commodo condimentum sit amet mollis dolor. Curabitur at tempus orci, congue hendrerit est. Nunc sed nulla at enim accumsan malesuada.\n" +
            "\n" +
            "Nam et velit vel neque condimentum congue sed ac arcu. Suspendisse congue malesuada tellus eget volutpat. In libero sem, tempus ut ante quis, porta porttitor leo. Fusce blandit neque in facilisis iaculis. Fusce vel quam nec est tempor laoreet. Donec ut ante velit. Maecenas suscipit luctus sollicitudin. Ut turpis mi, tristique vel nibh ullamcorper, sagittis placerat augue. Mauris sed quam ut mi gravida eleifend. Nullam viverra arcu viverra felis pellentesque, a feugiat ante fringilla. Donec et dolor eu nulla porta facilisis. Praesent a molestie turpis. Morbi sed ligula vel libero sagittis blandit. Vivamus id urna lorem.\n" +
            "\n" +
            "Nam ante elit, ornare non sapien id, fringilla imperdiet neque. Nunc luctus egestas placerat. Fusce ultricies egestas enim, non venenatis urna pulvinar ac. Duis non hendrerit turpis, quis malesuada neque. Donec ac tincidunt nisi. Cras efficitur rutrum lorem, pellentesque porta purus porta sit amet. Praesent sagittis auctor lorem eu pellentesque.\n" +
            "\n" +
            "Nulla vulputate, lectus ullamcorper pretium mattis, quam ex ultricies arcu, sed mollis lorem ligula a purus. Quisque magna ligula, viverra sollicitudin ante sed, aliquam volutpat dolor. Mauris commodo ut mauris ac varius. Suspendisse in cursus felis. Duis elit enim, rutrum vel mauris in, porta porta arcu. Sed blandit lorem id justo ultrices, vel vehicula arcu sagittis. Donec quis faucibus arcu, eget volutpat magna.\n" +
            "\n" +
            "Sed tempor nibh arcu, non hendrerit orci maximus et. Proin rhoncus, orci vitae convallis bibendum, lorem urna rutrum libero, ac facilisis massa nibh sit amet massa. Pellentesque auctor sed dui non cursus. Integer tincidunt placerat aliquet. Phasellus non sem id lectus ullamcorper lacinia. Suspendisse erat mauris, ornare in accumsan non, viverra ut purus. Duis pulvinar feugiat justo, eget ullamcorper neque. Vivamus facilisis aliquam metus, vitae vehicula lacus finibus ac. Nunc sodales libero viverra pulvinar posuere. Praesent non volutpat justo. Nullam molestie sapien elit, ac pellentesque ex scelerisque in. Sed convallis lobortis turpis et rutrum. Donec finibus dignissim arcu, sit amet pellentesque ante. In vitae libero maximus, viverra lorem eget, rutrum diam. Aenean est enim, dapibus quis pharetra in, fermentum volutpat enim. Suspendisse iaculis, odio vel ultrices commodo, felis elit imperdiet nisi, sit amet varius eros nisi at libero. Quisque lacinia nisl sed nisl vulputate viverra. Vestibulum.";

    @Before
    public void setUp() {
        QRInterface qrInterface = new QRCodeGenerator(null, null);
    }

    @Test
    public void compressDataTest() {
        QRCodeGenerator qr = new QRCodeGenerator(null, null);
        byte[] result = qr.compress(veryLongLorem);
        String uncompressed = qr.decompress(result);
        Assert.assertTrue(result.length < veryLongLorem.getBytes().length);
        Assert.assertTrue(uncompressed.equals(veryLongLorem));
    }
}
