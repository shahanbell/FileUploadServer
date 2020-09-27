/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lightshell.fileupload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author kevindong
 */
public class FileUploadServlet extends HttpServlet {

    private static final String CHARSET = "UTF-8";

    private final Logger log4j = LogManager.getLogger("com.lightshell.web");

    private String uploadPath;
    private String backupPath;
    private String url;

    private boolean backup = false;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet FileUploadServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet FileUploadServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param req servlet request
     * @param res servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding(CHARSET);
        res.setCharacterEncoding(CHARSET);
        res.setContentType("application/json");
        res.setHeader("Access-Control-Allow-Credentials", "true");
        res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        res.setHeader("Connection", req.getHeader("Connection"));
        // 授权信息
        if (req.getHeader("Authorization") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        PrintWriter writer = res.getWriter();
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);
        if (isMultipart) {
            try {
                String fileName, uuidName, fullName;
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setRepository(new File(this.uploadPath));
                // 创建一个ServletFileUpload核心对象
                ServletFileUpload uploadHandler = new ServletFileUpload(factory);
                uploadHandler.setHeaderEncoding(CHARSET);
                // 获取上传内容
                List<FileItem> items = uploadHandler.parseRequest(req);
                for (FileItem item : items) {
                    if (item.isFormField()) {
                        item.getFieldName();
                    } else {
                        fileName = item.getName();
                        uuidName = UUID.randomUUID().toString().replace("-", "") + fileName.substring(fileName.lastIndexOf("."));
                        fullName = this.uploadPath + uuidName;
                        File file = new File(fullName);
                        item.write(file);
                        item.delete();
                        JSONObject f = new JSONObject();
                        f.put("name", fileName);
                        f.put("uid", uuidName);
                        f.put("url", url + "/FileUploadServer/resources/" + uuidName);
                        //f.put("thumbUrl", url + "/FileUploadServer/resources/" + uuidName);
                        ja.put(f);
                        jo.put("files", ja);
                    }
                }
                jo.put("code", "200");
                jo.put("msg", "success");
                log4j.info(jo.toString());
            } catch (Exception ex) {
                jo.put("code", "500");
                jo.put("msg", ex.getMessage());
                log4j.error(ex);
            } finally {
                writer.write(jo.toString());
                writer.close();
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doOptions(req, resp);
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Access-Control-Allow-Headers", req.getHeader("Access-Control-Request-Headers"));
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("success");
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "FileUpload Servlet";
    }// </editor-fold>

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.uploadPath = config.getServletContext().getRealPath("/") + config.getInitParameter("UploadPath");
        this.backupPath = config.getInitParameter("BackupPath");
        File path;
        path = new File(this.uploadPath);
        if (!path.isDirectory()) {
            path.mkdirs();
        }
        if (!"none".equals(this.backupPath)) {
            path = new File(this.backupPath);
            if (!path.isDirectory()) {
                path.mkdirs();
            }
            backup = true;
        }
        this.url = config.getInitParameter("url");
    }

}
