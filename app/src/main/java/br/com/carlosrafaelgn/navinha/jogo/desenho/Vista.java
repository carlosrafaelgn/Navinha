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
package br.com.carlosrafaelgn.navinha.jogo.desenho;

import br.com.carlosrafaelgn.navinha.modelo.desenho.Tela;

public final class Vista {
	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	private Vista() {
		// Essa classe não precisa ser instanciada
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public static boolean isEstreita(FolhaDeSprites folhaDeSprites) {
		// Iremos considerar como estreita uma tela com menos de 15 unidades de altura
		return (folhaDeSprites.unidades(Tela.getTela().getAlturaDaVista()) < 15.0f);
	}

	public static void ajusteTela(FolhaDeSprites folhaDeSprites) {
		// Existem diversas configurações de tamanho de tela e densidade para os aparelhos Android
		// Considerando a altura da nave, e medindo as configurações mais comuns, cheguei à seguinte
		// conclusão: a tela da *vasta* maioria dos telefones possui de 13 a 17 naves de altura,
		// enquanto que as telas dos tablets chegam a 33 naves de altura
		// O problema é que com mais de 17 naves de altura, a tela começa a parecer muito grande,
		// pois fica com muitos espaços vazios, e com menos de 13 naves, fica muito "apertada"
		// Assim, nós vamos fazer uma regra de 3, alterando o tamanho da vista proporcionalmente
		// em casos onde a tela possua menos de 13 ou mais de 17 naves de altura

		Tela tela = Tela.getTela();

		float larguraDaTela = tela.getLarguraDaTela();
		float alturaDaTela = tela.getAlturaDaTela();

		float unidadesPorTela = folhaDeSprites.unidades(alturaDaTela);

		if (unidadesPorTela >= 13.0f && unidadesPorTela < 18.0f) {
			// Tudo certo, podemos deixar a vista com o mesmo tamanho da tela
			tela.setAlturaDaVista(alturaDaTela);
			tela.setLarguraDaVista(larguraDaTela);
		} else {
			// Teremos que redimensionar a vista proporcionalmente

			// A altura de 15 naves fica visualmente bastante confortável
			float alturaDaVista = folhaDeSprites.pixels(15.0f);

			// Vamos deixar a largura como sendo um número inteiro, e par
			float larguraDaVista = (float)Math.ceil(larguraDaTela * (alturaDaVista / alturaDaTela));
			if ((((int)larguraDaVista) & 1) != 0) {
				larguraDaVista++;
			}
			tela.setLarguraDaVista(larguraDaVista);
			tela.setAlturaDaVista(alturaDaVista);
		}
	}
}
