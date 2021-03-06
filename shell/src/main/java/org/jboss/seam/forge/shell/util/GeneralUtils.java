/*
 * JBoss, by Red Hat.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.seam.forge.shell.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jboss.seam.forge.project.services.ResourceFactory;
import org.jboss.seam.forge.resources.DirectoryResource;
import org.jboss.seam.forge.resources.Resource;
import org.jboss.seam.forge.shell.Shell;
import org.jboss.seam.forge.shell.ShellColor;
import org.jboss.seam.forge.shell.ShellPrintWriter;

/**
 * @author Mike Brock .
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class GeneralUtils
{
   public static <T> List<T> concatArraysToList(final T[]... arrays)
   {
      List<T> newList = new ArrayList<T>();
      for (T[] elArray : arrays)
      {
         newList.addAll(Arrays.asList(elArray));
      }

      return newList;
   }

   public static String elementListSimpleTypesToString(final List<Class<?>> list)
   {
      StringBuilder sbuild = new StringBuilder();
      for (int i = 0; i < list.size(); i++)
      {
         sbuild.append(list.get(0).getSimpleName());
         if (i < list.size())
         {
            sbuild.append(", ");
         }
      }
      return sbuild.toString();
   }

   public static String elementSetSimpleTypesToString(final Set<Class<?>> set)
   {
      StringBuilder sbuild = new StringBuilder();

      for (Iterator<Class<?>> iter = set.iterator(); iter.hasNext();)
      {
         sbuild.append(iter.next().getSimpleName());
         if (iter.hasNext())
         {
            sbuild.append(", ");
         }
      }
      return sbuild.toString();
   }

   public static class OutputAttributes
   {
      public OutputAttributes(final int columnSize, final int columns)
      {
         this.columnSize = columnSize;
         this.columns = columns;
      }

      private final int columnSize;
      private final int columns;
   }

   public static OutputAttributes calculateOutputAttributs(final List<String> rawList, final Shell shell,
                                                           final OutputAttributes in)
   {
      if (in == null)
      {
         return calculateOutputAttributs(rawList, shell);
      }

      OutputAttributes newAttr = calculateOutputAttributs(rawList, shell);

      return new OutputAttributes(in.columnSize > newAttr.columnSize ? in.columnSize : newAttr.columnSize,
            in.columns < newAttr.columns ? in.columns : newAttr.columns);
   }

   public static OutputAttributes calculateOutputAttributs(final List<String> rawList, final Shell shell)
   {
      int width = shell.getWidth();
      int maxLength = 0;

      for (String s : rawList)
      {
         if (s.length() > maxLength)
         {
            maxLength = s.length();
         }
      }
      int cols = width / (maxLength + 4);
      int colSize;

      if (cols == 0)
      {
         colSize = width;
         cols = 1;

      }
      else
      {
         colSize = width / cols;
      }

      return new OutputAttributes(colSize, cols);
   }

   public static void printOutColumns(final List<String> rawList, final ShellPrintWriter out, final Shell shell,
                                      final boolean sort)
   {
      printOutColumns(rawList, ShellColor.NONE, out, calculateOutputAttributs(rawList, shell), null, sort);
   }

   public static void printOutColumns(final List<String> rawList, final ShellPrintWriter out, final Shell shell,
                                      final FormatCallback callback, final boolean sort)
   {
      printOutColumns(rawList, ShellColor.NONE, out, calculateOutputAttributs(rawList, shell), callback, sort);
   }

   public static void printOutColumns(final List<String> rawList, final ShellColor color,
                                      final ShellPrintWriter printWriter,
                                      final OutputAttributes attributes, final FormatCallback callback,
                                      final boolean sort)
   {
      if (sort)
      {
         Collections.sort(rawList);
      }

      int cols = attributes.columns;
      int colSize = attributes.columnSize;

      int i = 0;
      for (String s : rawList)
      {
         String out = callback != null ? callback.format(0, s) : s;
         if (color == ShellColor.NONE)
         {
            printWriter.print(out);
         }
         else
         {
            printWriter.print(color, out);
         }

         if (++i != cols)
         {
            printWriter.print(pad(colSize - s.length()));
         }

         if (i == cols)
         {
            printWriter.println();
            i = 0;
         }
      }
      if ((i != 0) && (i != cols))
      {
         printWriter.println();
      }
   }

   public static void printOutTables(final List<String> list, final int cols, final Shell shell)
   {
      printOutTables(list, new boolean[cols], shell, null);
   }

   public static void printOutTables(final List<String> list, final boolean[] columns, final Shell shell)
   {
      printOutTables(list, columns, shell, null);
   }

   public static void printOutTables(final List<String> list, final boolean[] columns, final ShellPrintWriter shell,
                                     final FormatCallback callback)
   {
      int cols = columns.length;
      int[] colSizes = new int[columns.length];

      Iterator<String> iter = list.iterator();

      String el;
      while (iter.hasNext())
      {
         for (int i = 0; i < cols; i++)
         {
            if (colSizes[i] < (el = iter.next()).length())
            {
               colSizes[i] = el.length();
            }
         }
      }

      iter = list.iterator();

      while (iter.hasNext())
      {
         for (int i = 0; i < cols; i++)
         {
            el = iter.next();
            if (columns[i])
            {
               shell.print(pad(colSizes[i] - el.length()));
               if (callback != null)
               {
                  shell.print(callback.format(i, el));
               }
               else
               {
                  shell.print(el);
               }

               if (iter.hasNext())
               {
                  shell.print(" ");
               }
            }
            else
            {
               if (callback != null)
               {
                  shell.print(callback.format(i, el));
               }
               else
               {
                  shell.print(el);
               }

               if (iter.hasNext())
               {
                  shell.print(pad(colSizes[i] - el.length()));
                  shell.print(" ");
               }
            }
         }
         shell.println();
      }
   }

   public static String pad(final int amount)
   {
      char[] padding = new char[amount];
      for (int i = 0; i < amount; i++)
      {
         padding[i] = ' ';
      }
      return new String(padding);
   }

   public static Resource<?>[] parseSystemPathspec(final ResourceFactory resourceFactory,
                                                   final Resource<?> lastResource,
                                                   final Resource<?> currentResource, final String[] paths)
   {
      List<Resource<?>> result = new LinkedList<Resource<?>>();

      for (String path : paths)
      {
         if ("-".equals(path))
         {
            result.add(lastResource == null ? currentResource : lastResource);
         }
         else if (path == null)
         {
            result.add(new DirectoryResource(resourceFactory, new File(System.getProperty("user.home"))));
         }
         else
         {
            result.addAll(ResourceUtil.parsePathspec(resourceFactory, currentResource, path));
         }
      }

      return result.toArray(new Resource<?>[result.size()]);
   }

   public static String pathspecToRegEx(final String pathSpec)
   {
      StringBuilder sb = new StringBuilder("^");
      char c;
      for (int i = 0; i < pathSpec.length(); i++)
      {
         switch (c = pathSpec.charAt(i))
         {
         case '.':
            sb.append("\\.");
            break;
         case '*':
            sb.append(".*");
            break;
         case '?':
            sb.append(".");
            break;
         default:
            sb.append(c);
         }
      }

      return sb.append("$").toString();
   }
}
