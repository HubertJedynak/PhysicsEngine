package physicalTests;

import javafx.geometry.Point3D;
import physic.*;

import java.util.List;

public class Collisions2stary {

    public static CollisionDetectableElement findCollisionDetectableElement(Chain cZChwytakiem, Ball bChwytajaca, List<Chain> chainList){
        Ball b;
        Joint j;
        for(Chain c: chainList){
            if(c != cZChwytakiem){
                b = findFirstCollisionDetectableBall(cZChwytakiem,c,bChwytajaca);
                if( b != null ){
                    return new CollisionDetectableElement(c,b);
                }
                j = findFirstCollisionDetectableJoint(cZChwytakiem,c,bChwytajaca);
                if( j != null ){
                    return new CollisionDetectableElement(c,j);
                }
            }
        }
        return null;
    }
    // uwaga: wyeliminowac przypadek gdzie koliduje sama ze soba (chain)
    public static Ball findFirstCollisionDetectableBall(Chain cZChwytakiem, Chain c, Ball bChwytak){
        for(Ball i:c.ballsList){
            Point3D[] odcinekMiedzyKolidujacymikomponentami = Chains2BallsEnginesBezOsobliwosci.detectCollision(cZChwytakiem,c,bChwytak,i);
            if(odcinekMiedzyKolidujacymikomponentami != null){
                return i;
            }
        }
        return null;
    }

    public static Joint findFirstCollisionDetectableJoint(Chain cZChwytakiem, Chain c, Ball bChwytak){
        for(Joint i:c.jointList){
            Point3D[] odcinekMiedzyKolidujacymikomponentami = ChainsJointBallEnginesBezOsobliwosci.detectCollision(cZChwytakiem,c,bChwytak,i);
            if(odcinekMiedzyKolidujacymikomponentami != null){
                return i;
            }
        }
        return null;
    }



    public static void serveCollisionBetween2Chains(Chain c1, Chain c2) {

        for (Ball b1 : c1.ballsList) {
            for (Ball b2 : c2.ballsList) {
                Chains2BallsEnginesBezOsobliwosci.serveCollision(c1, c2, b1, b2);
            }
        }

        for (Ball b : c1.ballsList) {
            for (Joint j : c2.jointList) {
                ChainsJointBallEnginesBezOsobliwosci.serveCollision(c1, c2, b, j);
            }
        }

        for (Ball b : c2.ballsList) {
            for (Joint j : c1.jointList) {
                ChainsJointBallEnginesBezOsobliwosci.serveCollision(c2, c1, b, j);
            }
        }

        for (Joint j1 : c1.jointList) {
            for (Joint j2 : c2.jointList) {
                Chains2JointsEnginesBezOsobliwosci.serveCollision(c1, c2, j1, j2);
            }
        }

    }

    public static void serveAllCollisions(List<Chain> chainList) {

        for (int x = 0; x < chainList.size() - 1; x++) {
            for (int y = x + 1; y < chainList.size(); y++) {
                Chain c1 = chainList.get(x);
                Chain c2 = chainList.get(y);
                serveCollisionBetween2Chains(c1, c2);
            }
        }


    }

    //tego uzywac
    public static class Chains2JointsEnginesBezOsobliwosci {

        public static Point3D[] detectCollision(Chain c1, Chain c2, Joint j1, Joint j2) {

            Point3D vecj1 = new Point3D(
                    j1.b2.x.get() - j1.b1.x.get(),
                    j1.b2.y.get() - j1.b1.y.get(),
                    j1.b2.z.get() - j1.b1.z.get()
            );

            Point3D nvecj1 = vecj1.normalize();

            Point3D vecj2 = new Point3D(
                    j2.b2.x.get() - j2.b1.x.get(),
                    j2.b2.y.get() - j2.b1.y.get(),
                    j2.b2.z.get() - j2.b1.z.get()
            );

            Point3D nvecj2 = vecj2.normalize();

            Point3D vecsj1sj2 = new Point3D(
                    j2.b1.x.get() - j1.b1.x.get(),
                    j2.b1.y.get() - j1.b1.y.get(),
                    j2.b1.z.get() - j1.b1.z.get()
            );

            // wektor p - najkrotszy wektor laczocy j1 i j2
            // wyznaczam kierunak wektora p (wersor p)
            Point3D p = nvecj1.crossProduct(nvecj2).normalize();
            // sprawdzic czy rownolegle (jesli zerowy iloczyn wektorowy to liczymy odleglosc joint kula)
            //System.out.println("wektor p "+p);

            if (p.magnitude() == 0) {
                // System.out.println("wektory sa równolegle");
                return null;
                // jak wektory sa rownolegle to trzeba zakonczyc funkcje, bo problem rozwiaze kolizja ball joint
            }

            // "dlugosc" p (w cudzyslowiu, bo moze byc ujemna)
            double rzutVecsj1sj2naP = vecsj1sj2.dotProduct(p);
            //System.out.println("rzut wektora "+ rzutVecsj1sj2naP);
            // JESLI DLUGOSC JEST MNIEJSZA NIZ SUMA PROMIENI TO NIE MA KOLIZJI (TRZEBA ZROBIC MODOL ABS)
//            if(Math.abs(rzutVecsj1sj2naP) > j1.getRadius() + j2.getRadius()){
//            //    System.out.println("nie ma kolizji");
//                return null;
//            }else{
//            //    System.out.println("jest kolizja");
//                //System.out.println("problenik");
//                ////////////////////////////////////////////////////
//            }

            // licze wektor p
            p = p.multiply(rzutVecsj1sj2naP);

//            System.out.println("x"+p.getX()+"y"+p.getY()+"z"+p.getZ());
            // przesuwam j1 o wektor p do j2 (zeby sie przecinaly)
//
            double nx1 = j1.b1.x.get() + p.getX(); // nowy x poczatku jointa (pierwszej pilki)
            double ny1 = j1.b1.y.get() + p.getY();
            double nz1 = j1.b1.z.get() + p.getZ();

            // wektor miedzy nowym j1.b1 a starym j2.b1
            // potem poprawic zeby wektor p dodac
//            Point3D vecnsj2 = new Point3D(
//                    vecj2.getX() - nx1,
//                    vecj2.getY() - ny1,
//                    vecj2.getZ() - nz1
//            );

            Point3D vecnsj2 = new Point3D(
                    j2.b1.x.get() - nx1,
                    j2.b1.y.get() - ny1,
                    j2.b1.z.get() - nz1
            );

            // licze beta (wektor miedzy j2.b1 a punktem przeciecia na j2)

//
//            double gh1,gh2; // wersor miedzy nowoprzesunietymi krancami j1. Liczby 1 i 2 oznaczaja wspolrzedne x,y,z (domyslnie 1=x, 2=y)
//            double g1,g2; // nowoprzesuniety j1.b1
//            double cd1,cd2; // wersor miedzy krancami j2
//            double c1,c2; // j2.b1
            double beta = 0;
            if (p.getY() != 0) {
                //gdy py != 0
                //1=x, 2=z
                //    System.out.println("beta z y");
                beta = nvecj1.crossProduct(vecnsj2).getY() / nvecj2.crossProduct(nvecj1).getY();
                //System.out.println("beta= "+beta);
            } else if (p.getX() != 0) {
                //gdy px != 0
                //1=y, 2=z
                //System.out.println("beta z x");
                beta = nvecj1.crossProduct(vecnsj2).getX() / nvecj2.crossProduct(nvecj1).getX();
            } else {
                //domyslnie 1=x 2=y, czyli pz != 0
                //    System.out.println("beta z z");
                beta = nvecj1.crossProduct(vecnsj2).getZ() / nvecj2.crossProduct(nvecj1).getZ();
            }

            // szukany punkt na j2
            Point3D F = new Point3D(
                    j2.b1.x.get() + nvecj2.getX() * beta,
                    j2.b1.y.get() + nvecj2.getY() * beta,
                    j2.b1.z.get() + nvecj2.getZ() * beta
            );

            //Point3D E = F.add(p);
            // szukany punkt na j1
            Point3D E = F.subtract(p);
//            j1.b1.addToX(p.getX());
//            j1.b1.addToY(p.getY());
//            j1.b1.addToZ(p.getZ());
//
//            j1.b2.addToX(p.getX());
//            j1.b2.addToY(p.getY());
//            j1.b2.addToZ(p.getZ());


            // SPRAWDZENIE, CZY ZNALEZIONE PUNKTY NALEZA DO ODPOWIEDNICH ODCINKOW
            Point3D vecsj1E = E.subtract(j1.b1.x.get(), j1.b1.y.get(), j1.b1.z.get());
            if (nvecj1.dotProduct(vecsj1E) < 0 || nvecj1.dotProduct(vecsj1E) > vecj1.magnitude()) {
                //    System.out.println("punkt E jest poza jointem j1");
                return null;
            }

            Point3D vecsj2F = F.subtract(j2.b1.x.get(), j2.b1.y.get(), j2.b1.z.get());
            if (nvecj2.dotProduct(vecsj2F) < 0 || nvecj2.dotProduct(vecsj2F) > vecj2.magnitude()) {
                return null;
            }

            if (E.distance(F) > j1.getRadius() + j2.getRadius()) {
                return null;
            }

            //System.out.println("vecsj1E " + nvecj2.dotProduct(vecsj1E));
            //System.out.println("vecsj2F " + nvecj2.dotProduct(vecsj2F));

            //System.out.println("distance "+E.distance(F)+" > "+ (j1.getRadius() + j2.getRadius()));
            return new Point3D[]{E, F};

        }

        public static Point3D[] serveCollision(Chain c1, Chain c2, Joint j1, Joint j2) {

            Point3D stareW = c2.W;

            // WYKRYWANIE KOLIZJI MIEDZY DWOMA JOINTAMI
//            Joint j1 = c1.jointList.get(0);
//            Joint j2 = c2.jointList.get(0);

            // jesli zamiast odcinka jest null to nie ma kolizji
//            Point3D[] odcinekMiedzyJointami = Joints.serveCollisions(j1, j2);
            Point3D[] odcinekMiedzyJointami = Chains2JointsEnginesBezOsobliwosci.detectCollision(c1 ,c2 ,j1 ,j2);

            if (odcinekMiedzyJointami == null) {
                return null;
            }

            //System.out.println(odcinekMiedzyJointami[0]);
            //System.out.println(odcinekMiedzyJointami[1]);


            // WYZNACZANIE PARAMETROW (KROK 1 WIKIPEDIA)
            double dlugoscOdcinkaMiedzyJointami = odcinekMiedzyJointami[0].distance(odcinekMiedzyJointami[1]);
            // wersor n wskazuje od j1 do j2
            Point3D wersorN = (odcinekMiedzyJointami[1].subtract(odcinekMiedzyJointami[0])).normalize();
            //System.out.println(wersorN);
            double nachodzenieNaSiebie = j1.getRadius() + j2.getRadius() - dlugoscOdcinkaMiedzyJointami;

            // wyznaczenia punktu kolizji (uwzglednione rowniez odsuniecie obiektow zeby sie nie nakladaly)
            Point3D punktKolizji = odcinekMiedzyJointami[0].add(wersorN.multiply(j1.getRadius() - nachodzenieNaSiebie / 2));
            //System.out.println(punktKolizji);
            //odsuniecie obiektow zeby sie nie nakladaly
            c1.move(wersorN.multiply(-nachodzenieNaSiebie / 2));
            odcinekMiedzyJointami[0] = odcinekMiedzyJointami[0].add(wersorN.multiply(-nachodzenieNaSiebie / 2));
            c2.move(wersorN.multiply(nachodzenieNaSiebie / 2));
            odcinekMiedzyJointami[1] = odcinekMiedzyJointami[1].add(wersorN.multiply(nachodzenieNaSiebie / 2));

            // wyznaczanie r1 i r2 (wikipedia)
            Point3D r1 = punktKolizji.subtract(c1.centreOfGravity);
            Point3D r2 = punktKolizji.subtract(c2.centreOfGravity);
            //System.out.println("r1 "+r1);
            //System.out.println("r2 "+r2);

            // wyznaczenie Vr (wikipedia)
//            Point3D vp1 = c1.V.add(c1.W.crossProduct(r1));
//            Point3D vp2 = c2.V.add(c2.W.crossProduct(r2));
//            //System.out.println(getVCausedByEngines(c1,j1,punktKolizji));
            Point3D vp1 = c1.V.add(c1.W.crossProduct(r1)).add(getVCausedByEngines(c1, j1, punktKolizji));
            Point3D vp2 = c2.V.add(c2.W.crossProduct(r2)).add(getVCausedByEngines(c2, j2, punktKolizji));
            Point3D vr = vp2.subtract(vp1);
//            System.out.println("vp1 "+ vp1);
//            System.out.println("vp2 "+ vp2);
//            System.out.println("vr "+ vr);

//            System.out.println("getVCausedByEngines(c1,j1,punktKolizji) "+getVCausedByEngines(c1,j1,punktKolizji));
//            System.out.println("v "+c1.silnikPrzesuwnyList.get(0).V1);
            //System.out.println("getVCausedByEngines(c2,j2,punktKolizji) "+getVCausedByEngines(c2,j2,punktKolizji));


            // wyznaczanie I1 i I2
//            Point3D centrumUkladu = new Point3D(
//                    (c1.m * c1.centreOfGravity.getX() + c2.m * c2.centreOfGravity.getX()) / (c1.m + c2.m),
//                    (c1.m * c1.centreOfGravity.getY() + c2.m * c2.centreOfGravity.getY()) / (c1.m + c2.m),
//                    (c1.m * c1.centreOfGravity.getZ() + c2.m * c2.centreOfGravity.getZ()) / (c1.m + c2.m)
//            );
            Point3D sumaRM = new Point3D(0, 0, 0);
            double sumaM = 0;
            for (Ball b : c1.ballsList) {
                sumaRM = sumaRM.add(new Point3D(b.x.get(), b.y.get(), b.z.get()).multiply(b.m));
                sumaM += b.m;
            }
            for (Ball b : c2.ballsList) {
                sumaRM = sumaRM.add(new Point3D(b.x.get(), b.y.get(), b.z.get()).multiply(b.m));
                sumaM += b.m;
            }
            Point3D centrumUkladu = sumaRM.multiply(1 / sumaM);

            Matrix33 I1 = inertiaTensor(c1, punktKolizji);
            Matrix33 I2 = inertiaTensor(c2, punktKolizji);
            // System.out.println("I1"+I1);
            // System.out.println("I2"+I2);


            // wyznaczam impuls jr (wikipedia)

            double e = 0.5;
            double jr = (-(1 + e) * vr.dotProduct(wersorN)) /
                    (1 / c1.m + 1 / c2.m +
                            ((I1.inverseMultiply(r1.crossProduct(wersorN)).crossProduct(r1)).add(
                                    I2.inverseMultiply(r2.crossProduct(wersorN)).crossProduct(r2)
                            )).dotProduct(wersorN)
                    );

            //System.out.println(I1.inverseMultiply( r1.crossProduct(wersorN) ));


            // WYKRYWANIE OSOBLIWOSCI
            if (Double.isNaN(jr)) {
                // System.out.println("mamy osobliwosc "+jr);
                //USUNIECIE PUNKTOW OSOBLIWYCH
                I1.removeSingularPoints();
                I2.removeSingularPoints();

                jr = (-(1 + e) * vr.dotProduct(wersorN)) /
                        (1 / c1.m + 1 / c2.m +
                                ((I1.inverseMultiply(r1.crossProduct(wersorN)).crossProduct(r1)).add(
                                        I2.inverseMultiply(r2.crossProduct(wersorN)).crossProduct(r2)
                                )).dotProduct(wersorN)
                        );

            }

            // WEKTOR JR (KROK 2 WIKIPEDIA)
            //Point3D vecJr = wersorN.multiply(jr);
            // LICZENIE PREDKOSCI LINIOWYCH V
            c1.V = c1.V.add(wersorN.multiply(-jr / c1.m));
            c2.V = c2.V.add(wersorN.multiply(jr / c2.m));
            // LICZENIE PREDKOSCI KATOWYCH W
            c1.W = c1.W.add(I1.inverseMultiply(r1.crossProduct(wersorN)).multiply(-jr));
            c2.W = c2.W.add(I2.inverseMultiply(r2.crossProduct(wersorN)).multiply(jr));

            //   System.out.println("v1 "+c1.V);
            //   System.out.println("v2 "+c2.V);
            //   System.out.println("W1 "+c1.W);
            //   System.out.println("W2 "+c2.W);
            //   System.out.println("jr "+jr);

            //System.out.println("predkosc katowa "+c1.W);
            //System.out.println(c1.V);
            //System.out.println(c2.W.magnitude());
            //System.out.println(stareW.magnitude());

            c1.setBallsVCausedByChain();
            c2.setBallsVCausedByChain();

            return odcinekMiedzyJointami;

        }

        private static Matrix33 inertiaTensor(Chain c, Point3D punktKolizji) {

//            double modR = r.magnitude();
//            double[] vecR = {r.getX(),r.getY(),r.getZ()};
            double[][] I = new double[3][3];

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    double suma = 0;
                    for (Ball b : c.ballsList) {
                        //Point3D r = new Point3D(b.x.get(),b.y.get(),b.z.get()).subtract(c.centreOfGravity);
                        //Point3D r = new Point3D(b.x.get(),b.y.get(),b.z.get());
                        Point3D r = new Point3D(b.x.get(), b.y.get(), b.z.get()).subtract(punktKolizji);
                        double modR = r.magnitude();
                        double[] vecR = {r.getX(), r.getY(), r.getZ()};
                        suma += modR * modR * impulsDiracka(i, j) - vecR[i] * vecR[j];
                    }
                    I[i][j] = c.m * suma;
                }
            }
            return new Matrix33(I);
        }

        private static int impulsDiracka(int i, int j) {
            if (i != j) {
                return 0;
            } else {
                return 1;
            }
        }

        private static Point3D getVCausedByEngines(Chain c, Joint j, Point3D collisionPoint) {
            Point3D sumaV = Point3D.ZERO;
            for (RotaryMotor i : c.rotaryMotorList) {
                //  System.out.println("isActive w petli "+i.isActive);
                if (i.isActive) {
                    //    System.out.println("isActive");
                    if (i.jointList1.contains(j)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W1.crossProduct(r);
                        sumaV = sumaV.add(V);
                    } else if (i.jointList2.contains(j)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W2.crossProduct(r);
                        sumaV = sumaV.add(V);
                    }
                }
            }
            //System.out.println("suma "+sumaV);
            for (LinearMotor i : c.linearMotorList) {
                if (i.isActive) {
                    if (i.jointList1.contains(j)) {
                        sumaV = sumaV.add(i.V1);
                    } else if (i.jointList2.contains(j)) {
                        sumaV = sumaV.add(i.V2);
                    }
                }
            }
            return sumaV;
        }

    }

    //tego uzywac
    public static class ChainsJointBallEnginesBezOsobliwosci {

        public static Point3D[] detectCollision(Chain cb, Chain cj, Ball b, Joint j) {
            // interacts na początek zeby wyeliminowac przypadek ze b znajduje sie na prostej ale nie w odcinku (joinie)

            // znormalizowany wektor między koncami joina
            double dxj = (j.b2.x.get() - j.b1.x.get()) / j.getHeight();
            double dyj = (j.b2.y.get() - j.b1.y.get()) / j.getHeight();
            double dzj = (j.b2.z.get() - j.b1.z.get()) / j.getHeight();
            //wektor między "b1" (początkiem joina) a kula "b"
            double dxb = b.x.get() - j.b1.x.get();
            double dyb = b.y.get() - j.b1.y.get();
            double dzb = b.z.get() - j.b1.z.get();
            // dlugosc rzutu wektora b1b na odcinek (wektor) b1b2;
            double l = dxj * dxb + dyj * dyb + dzj * dzb;
            //System.out.println("l "+ l);
            if (l < 0 || l > j.getHeight()) {
                return null;
            }
            // wektor kuli "i" (kula "i" się pojawia na jedną iterację i zderza się z kulą "b" gdy dochodzi do kolizji);
            double dxi = dxj * l;
            double dyi = dyj * l;
            double dzi = dzj * l;
            //wspolrzedne kuli "i"
            double ix = j.b1.x.get() + dxi;
            double iy = j.b1.y.get() + dyi;
            double iz = j.b1.z.get() + dzi;

            Point3D vecBI = new Point3D(
                    ix - b.x.get(),
                    iy - b.y.get(),
                    iz - b.z.get()
            );

            if (vecBI.magnitude() > j.getRadius() + b.getRadius()) {
                return null;
            } else {
                return new Point3D[] { new Point3D(b.x.get(),b.y.get(),b.z.get()) , new Point3D(ix,iy,iz) };
            }

        }

        public static Point3D[] serveCollision(Chain cb, Chain cj, Ball b, Joint j) {

//            Ball b = cb.ballsList.get(0);
//            Joint j = cj.jointList.get(0);

            // jesli zamiast odcinka jest null to nie ma kolizji

            Point3D[] odcinekMiedzyJointemABallem = ChainsJointBallEnginesBezOsobliwosci.detectCollision(cb ,cj ,b ,j);
            if (odcinekMiedzyJointemABallem == null) {
                return null;
            }
            //System.out.println(odcinekMiedzyJointami[0]);
            //System.out.println(odcinekMiedzyJointami[1]);

            // WYZNACZANIE PARAMETROW (KROK 1 WIKIPEDIA)
            double dlugoscOdcinkaMiedzyJointami = odcinekMiedzyJointemABallem[0].distance(odcinekMiedzyJointemABallem[1]);
            // wersor n wskazuje od b do j
            Point3D wersorN = (odcinekMiedzyJointemABallem[1].subtract(odcinekMiedzyJointemABallem[0])).normalize();
            //System.out.println(wersorN);
            double nachodzenieNaSiebie = b.getRadius() + j.getRadius() - dlugoscOdcinkaMiedzyJointami;

            // wyznaczenia punktu kolizji (uwzglednione rowniez odsuniecie obiektow zeby sie nie nakladaly)
            Point3D punktKolizji = odcinekMiedzyJointemABallem[0].add(wersorN.multiply(b.getRadius() - nachodzenieNaSiebie / 2));
            //System.out.println(punktKolizji);
            //odsuniecie obiektow zeby sie nie nakladaly
            cb.move(wersorN.multiply(-nachodzenieNaSiebie / 2));
            odcinekMiedzyJointemABallem[0] = odcinekMiedzyJointemABallem[0].add(wersorN.multiply(-nachodzenieNaSiebie / 2));
            cj.move(wersorN.multiply(nachodzenieNaSiebie / 2));
            odcinekMiedzyJointemABallem[1] = odcinekMiedzyJointemABallem[1].add(wersorN.multiply(nachodzenieNaSiebie / 2));

            // wyznaczanie r1 i r2 (wikipedia)
            Point3D r1 = punktKolizji.subtract(cb.centreOfGravity);
            Point3D r2 = punktKolizji.subtract(cj.centreOfGravity);
            //System.out.println("r1 "+r1);
            //System.out.println("r2 "+r2);

            // wyznaczenie Vr (wikipedia)
            // 1 -
            // 2 -
            Point3D vp1 = cb.V.add(cb.W.crossProduct(r1)).add(getVCausedByEngines(cb, b, punktKolizji));
            Point3D vp2 = cj.V.add(cj.W.crossProduct(r2)).add(getVCausedByEngines(cj, j, punktKolizji));
            Point3D vr = vp2.subtract(vp1);
//            System.out.println("vp1 "+ vp1);
//            System.out.println("vp2 "+ vp2);
//            System.out.println("vr "+ vr);

            // wyznaczanie I1 i I2
            Matrix33 I1 = inertiaTensor(cb, punktKolizji);
            Matrix33 I2 = inertiaTensor(cj, punktKolizji);

            // wyznaczam impuls jr (wikipedia)

            double e = 0.5;
            double jr = (-(1 + e) * vr.dotProduct(wersorN)) /
                    (1 / cb.m + 1 / cj.m +
                            ((I1.inverseMultiply(r1.crossProduct(wersorN)).crossProduct(r1)).add(
                                    I2.inverseMultiply(r2.crossProduct(wersorN)).crossProduct(r2)
                            )).dotProduct(wersorN)
                    );


            // WYKRYWANIE OSOBLIWOSCI
            if (Double.isNaN(jr)) {
                //   System.out.println("mamy osobliwosc "+jr);
                //USUNIECIE PUNKTOW OSOBLIWYCH
                I1.removeSingularPoints();
                I2.removeSingularPoints();

                jr = (-(1 + e) * vr.dotProduct(wersorN)) /
                        (1 / cb.m + 1 / cj.m +
                                ((I1.inverseMultiply(r1.crossProduct(wersorN)).crossProduct(r1)).add(
                                        I2.inverseMultiply(r2.crossProduct(wersorN)).crossProduct(r2)
                                )).dotProduct(wersorN)
                        );

            }

            // WEKTOR JR (KROK 2 WIKIPEDIA)
            //Point3D vecJr = wersorN.multiply(jr);
            // LICZENIE PREDKOSCI LINIOWYCH V
            cb.V = cb.V.add(wersorN.multiply(-jr / cb.m));
            cj.V = cj.V.add(wersorN.multiply(jr / cj.m));
            // LICZENIE PREDKOSCI KATOWYCH W
            cb.W = cb.W.add(I1.inverseMultiply(r1.crossProduct(wersorN)).multiply(-jr));
            cj.W = cj.W.add(I2.inverseMultiply(r2.crossProduct(wersorN)).multiply(jr));
            //   System.out.println(cb.W);
            //System.out.println(cb.V);

            cb.setBallsVCausedByChain();
            cj.setBallsVCausedByChain();

            return odcinekMiedzyJointemABallem;

        }

        private static Matrix33 inertiaTensor(Chain c, Point3D punktKolizji) {

//            double modR = r.magnitude();
//            double[] vecR = {r.getX(),r.getY(),r.getZ()};
            double[][] I = new double[3][3];

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    double suma = 0;
                    for (Ball b : c.ballsList) {
                        //Point3D r = new Point3D(b.x.get(),b.y.get(),b.z.get()).subtract(c.centreOfGravity);
                        //Point3D r = new Point3D(b.x.get(),b.y.get(),b.z.get());
                        Point3D r = new Point3D(b.x.get(), b.y.get(), b.z.get()).subtract(punktKolizji);
                        double modR = r.magnitude();
                        double[] vecR = {r.getX(), r.getY(), r.getZ()};
                        suma += modR * modR * impulsDiracka(i, j) - vecR[i] * vecR[j];
                    }
                    I[i][j] = c.m * suma;
                }
            }
            return new Matrix33(I);
        }

        private static int impulsDiracka(int i, int j) {
            if (i != j) {
                return 0;
            } else {
                return 1;
            }
        }

        private static Point3D getVCausedByEngines(Chain c, Ball b, Point3D collisionPoint) {
            Point3D sumaV = Point3D.ZERO;
            for (RotaryMotor i : c.rotaryMotorList) {
                //  System.out.println("isActive w petli "+i.isActive);
                if (i.isActive) {
                    //     System.out.println("isActive");
                    if (i.ballsList1.contains(b)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W1.crossProduct(r);
                        sumaV = sumaV.add(V);
                    } else if (i.ballsList2.contains(b)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W2.crossProduct(r);
                        sumaV = sumaV.add(V);
                    }
                }
            }
            //System.out.println("suma "+sumaV);
            for (LinearMotor i : c.linearMotorList) {
                if (i.isActive) {
                    if (i.ballsList1.contains(b)) {
                        sumaV = sumaV.add(i.V1);
                    } else if (i.ballsList2.contains(b)) {
                        sumaV = sumaV.add(i.V2);
                    }
                }
            }
            return sumaV;
        }

        private static Point3D getVCausedByEngines(Chain c, Joint j, Point3D collisionPoint) {
            Point3D sumaV = Point3D.ZERO;
            for (RotaryMotor i : c.rotaryMotorList) {
                //  System.out.println("isActive w petli "+i.isActive);
                if (i.isActive) {
                    //    System.out.println("isActive");
                    if (i.jointList1.contains(j)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W1.crossProduct(r);
                        sumaV = sumaV.add(V);
                    } else if (i.jointList2.contains(j)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W2.crossProduct(r);
                        sumaV = sumaV.add(V);
                    }
                }
            }
            //System.out.println("suma "+sumaV);
            for (LinearMotor i : c.linearMotorList) {
                if (i.isActive) {
                    if (i.jointList1.contains(j)) {
                        sumaV = sumaV.add(i.V1);
                    } else if (i.jointList2.contains(j)) {
                        sumaV = sumaV.add(i.V2);
                    }
                }
            }
            return sumaV;
        }


    }

    //tego uzywac
    public static class Chains2BallsEnginesBezOsobliwosci {

        public static Point3D[] detectCollision(Chain c1, Chain c2, Ball b1, Ball b2) {
            Point3D vecb1 = new Point3D(
                    b1.x.get(),
                    b1.y.get(),
                    b1.z.get()
            );

            Point3D vecb2 = new Point3D(
                    b2.x.get(),
                    b2.y.get(),
                    b2.z.get()
            );


            if (vecb1.distance(vecb2) > b1.getRadius() + b2.getRadius()) {
                return null;
            } else {
                return new Point3D[] {vecb1,vecb2};
            }
        }

        public static Point3D[] serveCollision(Chain c1, Chain c2, Ball b1, Ball b2) {


            Point3D[] odcinekMiedzyKulami = Chains2BallsEnginesBezOsobliwosci.detectCollision(c1 ,c2 ,b1 ,b2);

            if (odcinekMiedzyKulami == null) {
                return null;
            }

            //  System.out.println("kula 1 "+odcinekMiedzyKulami[0]);
            //  System.out.println("kula 2 "+odcinekMiedzyKulami[1]);


            // WYZNACZANIE PARAMETROW (KROK 1 WIKIPEDIA)
            double dlugoscOdcinkaMiedzyJointami = odcinekMiedzyKulami[0].distance(odcinekMiedzyKulami[1]);
            // wersor n wskazuje od j1 do j2
            Point3D wersorN = (odcinekMiedzyKulami[1].subtract(odcinekMiedzyKulami[0])).normalize();
            //System.out.println(wersorN);
            double nachodzenieNaSiebie = b1.getRadius() + b2.getRadius() - dlugoscOdcinkaMiedzyJointami;

            // wyznaczenia punktu kolizji (uwzglednione rowniez odsuniecie obiektow zeby sie nie nakladaly)
            Point3D punktKolizji = odcinekMiedzyKulami[0].add(wersorN.multiply(b1.getRadius() - nachodzenieNaSiebie / 2));
            //System.out.println(punktKolizji);
            //odsuniecie obiektow zeby sie nie nakladaly
            c1.move(wersorN.multiply(-nachodzenieNaSiebie / 2));
            odcinekMiedzyKulami[0] = odcinekMiedzyKulami[0].add(wersorN.multiply(-nachodzenieNaSiebie / 2));
            c2.move(wersorN.multiply(nachodzenieNaSiebie / 2));
            odcinekMiedzyKulami[1] = odcinekMiedzyKulami[1].add(wersorN.multiply(nachodzenieNaSiebie / 2));

            // wyznaczanie r1 i r2 (wikipedia)
            Point3D r1 = punktKolizji.subtract(c1.centreOfGravity);
            Point3D r2 = punktKolizji.subtract(c2.centreOfGravity);
            //System.out.println("r1 "+r1);
            //System.out.println("r2 "+r2);

            // wyznaczenie Vr (wikipedia)
//            Point3D vp1 = c1.V.add(c1.W.crossProduct(r1));
//            Point3D vp2 = c2.V.add(c2.W.crossProduct(r2));
//            //System.out.println(getVCausedByEngines(c1,j1,punktKolizji));
            Point3D vp1 = c1.V.add(c1.W.crossProduct(r1)).add(getVCausedByEngines(c1, b1, punktKolizji));
            Point3D vp2 = c2.V.add(c2.W.crossProduct(r2)).add(getVCausedByEngines(c2, b2, punktKolizji));
            Point3D vr = vp2.subtract(vp1);
//            System.out.println("vp1 "+ vp1);
//            System.out.println("vp2 "+ vp2);
//            System.out.println("vr "+ vr);

//            System.out.println("getVCausedByEngines(c1,j1,punktKolizji) "+getVCausedByEngines(c1,j1,punktKolizji));
//            System.out.println("v "+c1.silnikPrzesuwnyList.get(0).V1);
            //System.out.println("getVCausedByEngines(c2,j2,punktKolizji) "+getVCausedByEngines(c2,j2,punktKolizji));


            // wyznaczanie I1 i I2
//            Point3D centrumUkladu = new Point3D(
//                    (c1.m * c1.centreOfGravity.getX() + c2.m * c2.centreOfGravity.getX()) / (c1.m + c2.m),
//                    (c1.m * c1.centreOfGravity.getY() + c2.m * c2.centreOfGravity.getY()) / (c1.m + c2.m),
//                    (c1.m * c1.centreOfGravity.getZ() + c2.m * c2.centreOfGravity.getZ()) / (c1.m + c2.m)
//            );
            Point3D sumaRM = new Point3D(0, 0, 0);
            double sumaM = 0;
            for (Ball b : c1.ballsList) {
                sumaRM = sumaRM.add(new Point3D(b.x.get(), b.y.get(), b.z.get()).multiply(b.m));
                sumaM += b.m;
            }
            for (Ball b : c2.ballsList) {
                sumaRM = sumaRM.add(new Point3D(b.x.get(), b.y.get(), b.z.get()).multiply(b.m));
                sumaM += b.m;
            }
            Point3D centrumUkladu = sumaRM.multiply(1 / sumaM);

            Matrix33 I1 = inertiaTensor(c1, punktKolizji);
            Matrix33 I2 = inertiaTensor(c2, punktKolizji);
            //  System.out.println("I1"+I1);
            //  System.out.println("I2"+I2);


            // wyznaczam impuls jr (wikipedia)

            double e = 0.5;
            double jr = (-(1 + e) * vr.dotProduct(wersorN)) /
                    (1 / c1.m + 1 / c2.m +
                            ((I1.inverseMultiply(r1.crossProduct(wersorN)).crossProduct(r1)).add(
                                    I2.inverseMultiply(r2.crossProduct(wersorN)).crossProduct(r2)
                            )).dotProduct(wersorN)
                    );

            //System.out.println(I1.inverseMultiply( r1.crossProduct(wersorN) ));


            // WYKRYWANIE OSOBLIWOSCI
            if (Double.isNaN(jr)) {
                //  System.out.println("mamy osobliwosc "+jr);
                //USUNIECIE PUNKTOW OSOBLIWYCH
                I1.removeSingularPoints();
                I2.removeSingularPoints();

                jr = (-(1 + e) * vr.dotProduct(wersorN)) /
                        (1 / c1.m + 1 / c2.m +
                                ((I1.inverseMultiply(r1.crossProduct(wersorN)).crossProduct(r1)).add(
                                        I2.inverseMultiply(r2.crossProduct(wersorN)).crossProduct(r2)
                                )).dotProduct(wersorN)
                        );
            }

            // WEKTOR JR (KROK 2 WIKIPEDIA)
            //Point3D vecJr = wersorN.multiply(jr);
            // LICZENIE PREDKOSCI LINIOWYCH V
            c1.V = c1.V.add(wersorN.multiply(-jr / c1.m));
            c2.V = c2.V.add(wersorN.multiply(jr / c2.m));
            // LICZENIE PREDKOSCI KATOWYCH W
            c1.W = c1.W.add(I1.inverseMultiply(r1.crossProduct(wersorN)).multiply(-jr));
            c2.W = c2.W.add(I2.inverseMultiply(r2.crossProduct(wersorN)).multiply(jr));

            //  System.out.println("v1 "+c1.V);
            // System.out.println("v2 "+c2.V);
            // System.out.println("W1 "+c1.W);
            // System.out.println("W2 "+c2.W);
            // System.out.println("jr "+jr);

            //System.out.println("predkosc katowa "+c1.W);
            //System.out.println(c1.V);
            //System.out.println(c2.W.magnitude());
            //System.out.println(stareW.magnitude());

            c1.setBallsVCausedByChain();
            c2.setBallsVCausedByChain();

            return odcinekMiedzyKulami;

        }

        private static Matrix33 inertiaTensor(Chain c, Point3D punktKolizji) {

//            double modR = r.magnitude();
//            double[] vecR = {r.getX(),r.getY(),r.getZ()};
            double[][] I = new double[3][3];

            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 3; i++) {
                    double suma = 0;
                    for (Ball b : c.ballsList) {
                        //Point3D r = new Point3D(b.x.get(),b.y.get(),b.z.get()).subtract(c.centreOfGravity);
                        //Point3D r = new Point3D(b.x.get(),b.y.get(),b.z.get());
                        Point3D r = new Point3D(b.x.get(), b.y.get(), b.z.get()).subtract(punktKolizji);
                        double modR = r.magnitude();
                        double[] vecR = {r.getX(), r.getY(), r.getZ()};
                        suma += modR * modR * impulsDiracka(i, j) - vecR[i] * vecR[j];
                    }
                    I[i][j] = c.m * suma;
                }
            }
            return new Matrix33(I);
        }

        private static int impulsDiracka(int i, int j) {
            if (i != j) {
                return 0;
            } else {
                return 1;
            }
        }

        private static Point3D getVCausedByEngines(Chain c, Ball b, Point3D collisionPoint) {
            Point3D sumaV = Point3D.ZERO;
            for (RotaryMotor i : c.rotaryMotorList) {
                //  System.out.println("isActive w petli "+i.isActive);
                if (i.isActive) {
                    //    System.out.println("isActive");
                    if (i.ballsList1.contains(b)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W1.crossProduct(r);
                        sumaV = sumaV.add(V);
                    } else if (i.ballsList2.contains(b)) {
                        Point3D r = new Point3D(
                                collisionPoint.getX() - i.bEngine.x.get(),
                                collisionPoint.getY() - i.bEngine.y.get(),
                                collisionPoint.getZ() - i.bEngine.z.get()
                        );
                        Point3D V = i.W2.crossProduct(r);
                        sumaV = sumaV.add(V);
                    }
                }
            }
            //System.out.println("suma "+sumaV);
            for (LinearMotor i : c.linearMotorList) {
                if (i.isActive) {
                    if (i.ballsList1.contains(b)) {
                        sumaV = sumaV.add(i.V1);
                    } else if (i.ballsList2.contains(b)) {
                        sumaV = sumaV.add(i.V2);
                    }
                }
            }
            return sumaV;
        }

    }

    public static class CollisionDetectableElement {
        Chain c;
        Ball b;
        Joint j;

        public CollisionDetectableElement(Chain c, Ball b){
            this.c=c;
            this.b = b;
            this.j =null;
        }

        public CollisionDetectableElement(Chain c, Joint j){
            this.c=c;
            this.b = null;
            this.j = j;
        }

    }

}
