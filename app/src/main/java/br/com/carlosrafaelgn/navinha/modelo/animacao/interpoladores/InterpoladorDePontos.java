//
// Navinha is distributed under the FreeBSD License
//
// Copyright (c) 2016, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.
//
// https://github.com/carlosrafaelgn/Navinha
//
package br.com.carlosrafaelgn.navinha.modelo.animacao.interpoladores;

import br.com.carlosrafaelgn.navinha.modelo.dados.imutavel.VetorFloat;
import br.com.carlosrafaelgn.navinha.modelo.desenho.Ponto;

public abstract class InterpoladorDePontos {
	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private final Ponto ultimoPontoInterpolado;
	private final VetorFloat coordenadasX, coordenadasY, intervalos;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	// pontosIntervalo deve ser sempre crescrente, seu primeiro valor deve ser 0, e seu último valor
	// deve ser 1!
	InterpoladorDePontos(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		if (intervalos == null) {
			// Essa validação não aparece no bloco else, pois a validação do primeiro e do
			// último valor dos intervalos já garante que existem pelo menos dois valores
			if (coordenadasX.comprimento() < 2) {
				throw new IllegalArgumentException("Pelo menos dois pontos são necessários");
			}
			// Todos devem ter o mesmo comprimento
			if (coordenadasX.comprimento() != coordenadasY.comprimento()) {
				throw new IllegalArgumentException("Comprimentos diferentes");
			}
			intervalos = crieIntervalosEspacadosPelaDistanciaEntreOsPontos(coordenadasX, coordenadasY);
		} else {
			// Por uma questão de desempenho, essas validações podem ser removidas depois do jogo
			// ter sido testado, e você ter certeza de que intervalos realmente atende aos critérios

			if (intervalos.primeiro() != 0.0f || intervalos.ultimo() != 1.0f) {
				throw new IllegalArgumentException("intervalos contém valores inválidos");
			}
			// Vamos verificar se todos os valores são maiores que seus predecessores
			for (int i = intervalos.comprimento() - 1; i > 0; i--) {
				if (intervalos.item(i) <= intervalos.item(i - 1)) {
					throw new IllegalArgumentException("intervalos contém valores inválidos");
				}
			}
			// Todos devem ter o mesmo comprimento
			if (intervalos.comprimento() != coordenadasX.comprimento() || intervalos.comprimento() != coordenadasY.comprimento()) {
				throw new IllegalArgumentException("Comprimentos diferentes");
			}
		}

		this.ultimoPontoInterpolado = new Ponto(coordenadasX.primeiro(), coordenadasY.primeiro());
		this.coordenadasX = coordenadasX;
		this.coordenadasY = coordenadasY;
		this.intervalos = intervalos;
	}

	InterpoladorDePontos(InterpoladorDePontos original) {
		// Mantém uma cópia de todos os valores do interpolador original
		this.ultimoPontoInterpolado = original.getUltimoPontoInterpolado();
		this.coordenadasX = original.getCoordenadasX();
		this.coordenadasY = original.getCoordenadasY();
		this.intervalos = original.getIntervalos();
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	public final Ponto getUltimoPontoInterpolado() {
		return ultimoPontoInterpolado;
	}

	public final VetorFloat getCoordenadasX() {
		return coordenadasX;
	}

	public final VetorFloat getCoordenadasY() {
		return coordenadasY;
	}

	public final VetorFloat getIntervalos() {
		return intervalos;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos privados e protegidos
	//----------------------------------------------------------------------------------------------

	private static VetorFloat crieIntervalosEspacadosPelaDistanciaEntreOsPontos(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		float[] distancias = new float[coordenadasX.comprimento()];

		// O primeiro ponto está no início
		distancias[0] = 0.0f;

		float distanciaTotal = 0.0f;

		for (int p = 1; p < distancias.length; p++) {
			// Calcula a distância euclidiana entre o ponto p, e o ponto p - 1
			float deltaX = coordenadasX.item(p) - coordenadasX.item(p - 1);
			float deltaY = coordenadasY.item(p) - coordenadasY.item(p - 1);
			float distancia = (float)Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));

			// Se a distância for muito pequena, ou até mesmo 0, alguns interpoladores podem
			// produzir resultados ruins
			if (distancia < 0.0001f) {
				distancia = 0.0001f;
			}
			distanciaTotal += distancia;

			distancias[p] = distanciaTotal;
		}

		// Agora vamos transformar os valores em distancias para intervalos (que vão de 0 a 1)

		// O último intervalo é fácil ;)
		distancias[distancias.length - 1] = 1.0f;

		// O primeiro e o último intervalo já foram preenchidos
		for (int d = distancias.length - 2; d >= 1; d--) {
			distancias[d] /= distanciaTotal;
		}

		return new VetorFloat(distancias);
	}

	final int localizeIndiceDoIntervalo(float intervalo) {
		VetorFloat intervalos = getIntervalos();
		VetorFloat coordenadasX = getCoordenadasX();
		VetorFloat coordenadasY = getCoordenadasY();

		// Os dois casos especiais (primeiro e último pontos)
		if (intervalo <= 0.0f) {
			Ponto ponto = getUltimoPontoInterpolado();
			ponto.setX(coordenadasX.primeiro());
			ponto.setY(coordenadasY.primeiro());
			return -1;
		} else if (intervalo >= 1.0f) {
			Ponto ponto = getUltimoPontoInterpolado();
			ponto.setX(coordenadasX.ultimo());
			ponto.setY(coordenadasY.ultimo());
			return -1;
		}

		// Para os pontos intermediários, visto que intervalos está em ordem crescente, é possível
		// utilizar uma versão da busca binária para localizar o índice do intervalo em questão
		int inicio = 0, fim = intervalos.comprimento() - 1, meio;
		do {
			meio = (fim + inicio) >> 1;
			if (intervalo < intervalos.item(meio)) {
				fim = meio - 1;
			} else if (intervalo >= intervalos.item(meio + 1)) {
				inicio = meio + 1;
			} else {
				// Encontramos!
				break;
			}
		} while (fim >= inicio);

		return meio;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	// Repare que as classes InterpoladorDePontosLinear e InterpoladorDePontosAceleradoDesacelerado
	// poderiam ser substituídas pela classe InterpoladorDePontosSimples, utilizando a mesma técnica
	// que é mostrada em getAcelerado(), getDesacelerado() e getDegrau()
	// Contudo, estou mantendo as três classes para demonstrar três diferentes técnicas:
	// - InterpoladorDePontosLinear: utilizando coeficientes calculados de antemão
	// - InterpoladorDePontosAceleradoDesacelerado: sem coeficientes calculados de antemão
	// - InterpoladorDePontosSimples: reutilizando outras classes já existentes
	// Mesmo com todas essas técnicas, ainda existem outras, como por exemplo, fazer com que o
	// InterpoladorDePontosSimples possuísse dois InterpoladorDeValores, de maneira análoga ao que a
	// classe InterpoladorDePontosSpline faz
	public static InterpoladorDePontos crieConstante(float x, float y) {
		return new InterpoladorDePontosConstante(x, y);
	}

	public static InterpoladorDePontos crieLinear(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		return new InterpoladorDePontosLinear(coordenadasX, coordenadasY, crieIntervalosEspacadosPelaDistanciaEntreOsPontos(coordenadasX, coordenadasY));
	}

	public static InterpoladorDePontos crieLinear(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		return new InterpoladorDePontosLinear(coordenadasX, coordenadasY, intervalos);
	}

	public static InterpoladorDePontos crieAcelerado(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		return new InterpoladorDePontosSimples(Interpolador.crieAcelerado(), coordenadasX, coordenadasY, null);
	}

	public static InterpoladorDePontos crieAcelerado(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		return new InterpoladorDePontosSimples(Interpolador.crieAcelerado(), coordenadasX, coordenadasY, intervalos);
	}

	public static InterpoladorDePontos crieDesacelerado(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		return new InterpoladorDePontosSimples(Interpolador.crieDesacelerado(), coordenadasX, coordenadasY, null);
	}

	public static InterpoladorDePontos crieDesacelerado(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		return new InterpoladorDePontosSimples(Interpolador.crieDesacelerado(), coordenadasX, coordenadasY, intervalos);
	}

	public static InterpoladorDePontos crieAceleradoDesacelerado(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		return new InterpoladorDePontosAceleradoDesacelerado(coordenadasX, coordenadasY, null);
	}

	public static InterpoladorDePontos crieAceleradoDesacelerado(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		return new InterpoladorDePontosAceleradoDesacelerado(coordenadasX, coordenadasY, intervalos);
	}

	public static InterpoladorDePontos crieDegrau(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		return new InterpoladorDePontosSimples(Interpolador.crieDegrau(), coordenadasX, coordenadasY, null);
	}

	public static InterpoladorDePontos crieDegrau(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		return new InterpoladorDePontosSimples(Interpolador.crieDegrau(), coordenadasX, coordenadasY, intervalos);
	}

	public static InterpoladorDePontos crieSpline(VetorFloat coordenadasX, VetorFloat coordenadasY) {
		return new InterpoladorDePontosSpline(coordenadasX, coordenadasY, null);
	}

	public static InterpoladorDePontos crieSpline(VetorFloat coordenadasX, VetorFloat coordenadasY, VetorFloat intervalos) {
		return new InterpoladorDePontosSpline(coordenadasX, coordenadasY, intervalos);
	}

	public static InterpoladorDePontos crieInversor(InterpoladorDePontos original) {
		return new InterpoladorDePontosInversor(original);
	}

	// Por comodidade, o ponto retornado aqui é o mesmo retornado por getUltimoPontoInterpolado()
	public abstract Ponto interpole(float intervalo);
}
