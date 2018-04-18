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
package br.com.carlosrafaelgn.navinha.modelo.dados.persistencia;

import android.util.SparseArray;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import br.com.carlosrafaelgn.navinha.modelo.jogo.Jogo;

//--------------------------------------------------------------------------------------------------
// A classe ArmazenamentoPersistente é baseada na classe SerializableMap do meu outro projeto, o
// player de música de código livre FPlay, que pode ser encontrado na íntegra aqui, dentro do pacote
// br.com.carlosrafaelgn.fplay.util:
// https://github.com/carlosrafaelgn/FPlayAndroid
//--------------------------------------------------------------------------------------------------
public final class ArmazenamentoPersistente {
	//----------------------------------------------------------------------------------------------
	// Constantes
	//----------------------------------------------------------------------------------------------

	private static final int TIPO_BYTE = 0;
	private static final int TIPO_SHORT = 1;
	private static final int TIPO_INT = 2;
	private static final int TIPO_LONG = 3;
	private static final int TIPO_FLOAT = 4;
	private static final int TIPO_DOUBLE = 5;
	private static final int TIPO_STRING = 6;
	private static final int TIPO_BYTES = 7;
	private static final int TIPO_BITS = 8;

	//----------------------------------------------------------------------------------------------
	// Campos privados
	//----------------------------------------------------------------------------------------------

	private SparseArray<Object> armazenamento;
	private int contagemDeBits;
	private byte[] bits;

	//----------------------------------------------------------------------------------------------
	// Construtores
	//----------------------------------------------------------------------------------------------

	public ArmazenamentoPersistente() {
		// Cria o armazenamento já com capacidade para 64 valores
		setArmazenamento(new SparseArray<>(64));
	}

	//----------------------------------------------------------------------------------------------
	// Métodos acessores e modificadores
	//----------------------------------------------------------------------------------------------

	private SparseArray<Object> getArmazenamento() {
		return armazenamento;
	}

	private void setArmazenamento(SparseArray<Object> armazenamento) {
		this.armazenamento = armazenamento;
	}

	private int getContagemDeBits() {
		return contagemDeBits;
	}

	private void setContagemDeBits(int contagemDeBits) {
		this.contagemDeBits = contagemDeBits;
	}

	private byte[] getBits() {
		return bits;
	}

	private void setBits(byte[] bits) {
		this.bits = bits;
	}

	//----------------------------------------------------------------------------------------------
	// Métodos públicos
	//----------------------------------------------------------------------------------------------

	public boolean graveEmArquivo(String nomeDoArquivo) {
		FileOutputStream streamDoArquivo = null;
		BufferedOutputStream streamDeDestino = null;
		DataOutputStream streamDeDados = null;

		try {
			streamDoArquivo = Jogo.getJogo().getContext().openFileOutput(nomeDoArquivo, 0);
			streamDeDestino = new BufferedOutputStream(streamDoArquivo);
			streamDeDados = new DataOutputStream(streamDeDestino);

			// Ordem dos valores salvos:
			// tipo (byte)
			// chave (int)
			// conteúdo

			// O primeiro conteúdo que será gravado no arquivo será o conjunto de bits
			int contagemDeBits = getContagemDeBits();
			if (contagemDeBits > 0) {
				streamDeDados.write(TIPO_BITS);
				streamDeDados.writeInt(contagemDeBits);
				streamDeDados.write(getBits(), 0, (contagemDeBits + 7) >>> 3);
			}

			// Depois do conjunto de bits, grava todos os outros conteúdos

			SparseArray<Object> armazenamento = getArmazenamento();

			// Percorre em ordem crescente, pois as chaves são armazenadas em ordem crescente dentro
			// do SparseArray
			for (int i = 0; i < armazenamento.size(); i++) {

				int chave = armazenamento.keyAt(i);
				Object valor = armazenamento.get(chave);

				if (valor == null) {
					// Não há nada para salvar nessa chave
					continue;
				}

				if (valor instanceof Byte) {
					streamDeDados.write(TIPO_BYTE);
					streamDeDados.writeInt(chave);
					streamDeDados.write((Byte)valor);
				} else if (valor instanceof Short) {
					streamDeDados.write(TIPO_SHORT);
					streamDeDados.writeInt(chave);
					streamDeDados.writeShort((Short)valor);
				} else if (valor instanceof Integer) {
					streamDeDados.write(TIPO_INT);
					streamDeDados.writeInt(chave);
					streamDeDados.writeInt((Integer)valor);
				} else if (valor instanceof Long) {
					streamDeDados.write(TIPO_LONG);
					streamDeDados.writeInt(chave);
					streamDeDados.writeLong((Long)valor);
				} else if (valor instanceof Float) {
					streamDeDados.write(TIPO_FLOAT);
					streamDeDados.writeInt(chave);
					streamDeDados.writeFloat((Float)valor);
				} else if (valor instanceof Double) {
					streamDeDados.write(TIPO_DOUBLE);
					streamDeDados.writeInt(chave);
					streamDeDados.writeDouble((Double)valor);
				} else if (valor instanceof String) {
					streamDeDados.write(TIPO_STRING);
					streamDeDados.writeInt(chave);
					streamDeDados.writeUTF(valor.toString());
				} else {
					streamDeDados.write(TIPO_BYTES);
					streamDeDados.writeInt(chave);
					byte[] bytes = (byte[])valor;
					streamDeDados.writeInt(bytes.length);
					streamDeDados.write(bytes, 0, bytes.length);
				}
			}

			// Envia todos os dados para o arquivo
			streamDeDados.flush();

			return true;
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			// Apesar de streamDeDados.close() já fechar todos os outros streams, no caso de
			// algo dar errado streamDeDados pode ser null e algum dos outros dois streams não...
			try {
				if (streamDeDados != null)
					streamDeDados.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			try {
				if (streamDeDestino != null)
					streamDeDestino.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			try {
				if (streamDoArquivo != null)
					streamDoArquivo.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		return false;
	}

	public static ArmazenamentoPersistente carregueDoArquivo(String nomeDoArquivo) {
		FileInputStream streamDoArquivo = null;
		BufferedInputStream streamDeOrigem = null;
		DataInputStream streamDeDados = null;

		try {
			streamDoArquivo = Jogo.getJogo().getContext().openFileInput(nomeDoArquivo);
			streamDeOrigem = new BufferedInputStream(streamDoArquivo);
			streamDeDados = new DataInputStream(streamDeOrigem);

			ArmazenamentoPersistente armazenamentoPersistente = new ArmazenamentoPersistente();

			int tipo;

			while ((tipo = streamDeDados.read()) >= 0) {
				switch (tipo) {
				case TIPO_BYTE:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readByte());
					break;
				case TIPO_SHORT:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readShort());
					break;
				case TIPO_INT:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readInt());
					break;
				case TIPO_LONG:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readLong());
					break;
				case TIPO_FLOAT:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readFloat());
					break;
				case TIPO_DOUBLE:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readDouble());
					break;
				case TIPO_STRING:
					armazenamentoPersistente.put(streamDeDados.readInt(), streamDeDados.readUTF());
					break;
				case TIPO_BYTES:
					int chave = streamDeDados.readInt();
					int comprimento = streamDeDados.readInt();
					byte[] bytes = new byte[comprimento];

					if (comprimento > 0) {
						streamDeDados.readFully(bytes, 0, bytes.length);
					}

					armazenamentoPersistente.put(chave, bytes);
					break;
				case TIPO_BITS:
					int contagemDeBits = streamDeDados.readInt();
					byte[] bits = new byte[(contagemDeBits + 7) >>> 3];

					armazenamentoPersistente.setContagemDeBits(contagemDeBits);
					armazenamentoPersistente.setBits(bits);

					if (contagemDeBits > 0) {
						streamDeDados.readFully(bits, 0, bits.length);
					}
					break;
				default:
					// Um tipo desconhecido.... melhor encerrar por aqui
					return armazenamentoPersistente;
				}
			}

			return armazenamentoPersistente;
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			// Apesar de streamDeDados.close() já fechar todos os outros streams, no caso de
			// algo dar errado streamDeDados pode ser null e algum dos outros dois streams não...
			try {
				if (streamDeDados != null)
					streamDeDados.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			try {
				if (streamDeOrigem != null)
					streamDeOrigem.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			try {
				if (streamDoArquivo != null)
					streamDoArquivo.close();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	public void putBit(int indiceDoBit, boolean valor) {
		int contagemDeBits = getContagemDeBits();
		if (contagemDeBits <= indiceDoBit) {
			contagemDeBits = indiceDoBit + 1;
			setContagemDeBits(contagemDeBits);
		}

		int i = ((contagemDeBits + 7) >>> 3);
		byte[] bits = getBits();

		if (bits == null) {
			bits = new byte[i + 8];
			setBits(bits);
		} else if (bits.length < i) {
			final byte[] tmp = new byte[i + 8];
			System.arraycopy(bits, 0, tmp, 0, bits.length);
			bits = tmp;
			setBits(bits);
		}

		i = (indiceDoBit >>> 3);
		indiceDoBit = 1 << (indiceDoBit & 7);

		if (valor) {
			bits[i] |= indiceDoBit;
		} else {
			bits[i] &= ~indiceDoBit;
		}
	}

	public boolean getBit(int indiceDoBit) {
		return (indiceDoBit < getContagemDeBits()) && ((getBits()[indiceDoBit >>> 3] & (1 << (indiceDoBit & 7))) != 0);
	}

	public boolean getBit(int indiceDoBit, boolean valorPadrao) {
		if (indiceDoBit >= getContagemDeBits()) {
			return valorPadrao;
		}
		return ((getBits()[indiceDoBit >>> 3] & (1 << (indiceDoBit & 7))) != 0);
	}

	public Object get(int chave) {
		return getArmazenamento().get(chave);
	}

	public Object get(int chave, Object valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		return ((o == null) ? valorPadrao : o);
	}

	public byte getByte(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Byte)) {
			return 0;
		}
		return (Byte)o;
	}

	public byte getByte(int chave, byte valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Byte)) {
			return valorPadrao;
		}
		return (Byte)o;
	}

	public short getShort(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Short)) {
			return 0;
		}
		return (Short)o;
	}

	public short getShort(int chave, short valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Short)) {
			return valorPadrao;
		}
		return (Short)o;
	}

	public int getInt(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Integer)) {
			return 0;
		}
		return (Integer)o;
	}

	public int getInt(int chave, int valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Integer)) {
			return valorPadrao;
		}
		return (Integer)o;
	}

	public long getLong(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Long)) {
			return 0;
		}
		return (Long)o;
	}

	public long getLong(int chave, long valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Long)) {
			return valorPadrao;
		}
		return (Long)o;
	}

	public float getFloat(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Float)) {
			return 0;
		}
		return (Float)o;
	}

	public float getFloat(int chave, float valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Float)) {
			return valorPadrao;
		}
		return (Float)o;
	}

	public double getDouble(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Double)) {
			return 0;
		}
		return (Double)o;
	}

	public double getDouble(int chave, double valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof Double)) {
			return valorPadrao;
		}
		return (Double)o;
	}

	public String getString(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof String)) {
			return null;
		}
		return o.toString();
	}

	public String getString(int chave, String valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof String)) {
			return valorPadrao;
		}
		return o.toString();
	}

	public byte[] getBytes(int chave) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof byte[])) {
			return null;
		}
		return (byte[])o;
	}

	public byte[] getBytes(int chave, byte[] valorPadrao) {
		final Object o = getArmazenamento().get(chave);
		if (o == null || !(o instanceof byte[])) {
			return valorPadrao;
		}
		return (byte[])o;
	}

	public void put(int chave, byte valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, short valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, int valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, long valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, float valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, double valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, String valor) {
		getArmazenamento().put(chave, valor);
	}

	public void put(int chave, byte[] valor) {
		getArmazenamento().put(chave, valor);
	}

	public boolean contem(int chave) {
		return (getArmazenamento().indexOfKey(chave) >= 0);
	}

	public void remova(int chave) {
		getArmazenamento().remove(chave);
	}

	public void destrua() {
		// Vamos liberar toda a memória que não será mais utilizada e invalidar o objeto

		SparseArray<Object> armazenamento = getArmazenamento();
		if (armazenamento != null) {
			armazenamento.clear();
			setArmazenamento(null);
		}
		setBits(null);
		setContagemDeBits(0);
	}
}
