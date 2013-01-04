// ---------------------------------------------------------------------------
// Copyright 2012 Mauktik Gandhi
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ---------------------------------------------------------------------------
package resourceMonitor.parsers;

public class Token 
{
	public int start;
	public int end;
	
	public Token(int start, int end)
	{
		this.start = start;
		this.end = end;
	}
	
	public static Token getNextToken(String line, int start)
	{
		return Token.getNextToken(line, start, new Token(0, 0));
	}
	
	public static Token getNextToken(String line, int start, Token token)
	{
		int tokenStart = getTokenStart(line, start);
		
		if (tokenStart == -1)
		{
			return null;
		}
		
		int tokenEnd = getTokenEnd(line, tokenStart);
		
		if (tokenEnd == -1)
		{
			return null;
		}
		
		token.start = tokenStart;
		token.end = tokenEnd;
		return token;
	}
	
	public static Token getNthNextToken(String line, int start, int n)
	{
		return Token.getNthNextToken(line, start, n, new Token(0, 0));
	}
	
	public static Token getNthNextToken(String line, int start, int n, Token token)
	{
		while ((n > 0) && (token = getNextToken(line, start, token)) != null)
		{
			start = token.end + 1;
			n--;
		}
		
		return token;
	}
	
	private static int getTokenStart(String line, int start)
	{
		int value = start;
		while (value < line.length() && line.charAt(value) == ' ')
		{
			++value;
		}

		return value == line.length() ? -1 : value;
	}
	
	
	public static int getTokenEnd(String line, int start)
	{
		if (start >= line.length())
		{
			return -1;
		}
		
		int value = start;
		while (value < line.length() && line.charAt(value) != ' ')
		{
			++value;
		}

		return value - 1;
	}	
}
