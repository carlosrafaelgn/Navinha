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
package br.com.carlosrafaelgn.navinha.modelo.desenho;

public final class AlinhamentoDoPivo {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	public static final int HORIZONTAL_ESQUERDO = 1;
	public static final int HORIZONTAL_CENTRO = 2;
	public static final int HORIZONTAL_DIREITO = 4;

	public static final int VERTICAL_CIMA = 8;
	public static final int VERTICAL_CENTRO = 16;
	public static final int VERTICAL_BAIXO = 32;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	private AlinhamentoDoPivo() {
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public static float calculePivoXDoModelo(int alinhamentoDoPivo, float largura) {
		if ((alinhamentoDoPivo & HORIZONTAL_ESQUERDO) != 0) {
			return 0.0f;
		} else if ((alinhamentoDoPivo & HORIZONTAL_CENTRO) != 0) {
			return (0.5f * largura);
		} else if ((alinhamentoDoPivo & HORIZONTAL_DIREITO) != 0) {
			return largura;
		}

		throw new IllegalArgumentException("alinhamentoDoPivo não especifica um alinhamento horizontal");
	}

	public static float calculePivoYDoModelo(int alinhamentoDoPivo, float altura) {
		if ((alinhamentoDoPivo & VERTICAL_CIMA) != 0) {
			return 0.0f;
		} else if ((alinhamentoDoPivo & VERTICAL_CENTRO) != 0) {
			return (0.5f * altura);
		} else if ((alinhamentoDoPivo & VERTICAL_BAIXO) != 0) {
			return altura;
		}

		throw new IllegalArgumentException("alinhamentoDoPivo não especifica um alinhamento vertical");
	}
}
