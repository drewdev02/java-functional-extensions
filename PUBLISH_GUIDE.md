# 🚀 Guía de Publicación - Maven Central

## ✅ Archivos Creados

- ✅ `LICENSE` - MIT License
- ✅ `pom.xml` - Configurado para Maven Central
- ✅ README.md - Con documentación completa

---

## 📋 **Pasos para Publicar en Maven Central**

### **Paso 1: Crear Cuenta en Sonatype (5 minutos)**

1. Ir a https://issues.sonatype.org
2. Click en "Sign Up"
3. Crear cuenta con tu email

### **Paso 2: Crear Issue para Namespace (10 minutos)**

1. Ir a https://issues.sonatype.org/projects/OSSRH/issues
2. Click en "Create" issue
3. Completar:
   - **Project**: OSSRH (OSS Repository Hosting)
   - **Issue Type**: New Project
   - **Summary**: `com.adrewdev - java-functional-extensions`
   - **Description**:
     ```
     Group Id: com.adrewdev
     Project URL: https://github.com/adrewdev/java-functional-extensions
     SCM URL: https://github.com/adrewdev/java-functional-extensions.git
     ```
   - **Labels**: `com.adrewdev`

4. Click en "Create"

**Esperar aprobación**: 24-48 horas (te llega un email)

### **Paso 3: Configurar Credenciales (5 minutos)**

Una vez aprobado el issue:

1. Ir a https://s01.oss.sonatype.org
2. Loguearse con cuenta de Sonatype
3. Ir a "Profile" → "User Token"
4. Click en "Access User Token"
5. Copiar **Username** y **Password** (son diferentes a tu login!)

### **Paso 4: Configurar Maven Settings (5 minutos)**

Crear/editar `~/.m2/settings.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>ossrh</id>
            <username>TU_USERNAME_DE_SONATYPE</username>
            <password>TU_PASSWORD_DE_SONATYPE</password>
        </server>
    </servers>
</settings>
```

### **Paso 5: Generar GPG Key (10 minutos)**

**En macOS/Linux:**

```bash
# Instalar GPG si no lo tenés
brew install gpg  # macOS
# o
sudo apt-get install gnupg  # Linux

# Generar key
gpg --gen-key

# Te va a pedir:
# - Nombre: Andrew Developer
# - Email: tu.email@example.com
# - Passphrase: (guardala bien!)
```

**Exportar la key:**

```bash
# Listar keys
gpg --list-keys

# Exportar public key
gpg --keyserver keyserver.ubuntu.com --send-keys TU_KEY_ID

# Ejemplo:
# gpg --keyserver keyserver.ubuntu.com --send-keys 0x1234567890ABCDEF
```

### **Paso 6: Subir Proyecto a GitHub (5 minutos)**

```bash
# Crear repositorio en github.com (nuevo repo vacío)

# Agregar remote
git remote add origin https://github.com/adrewdev/java-functional-extensions.git

# Hacer push
git branch -M main
git push -u origin main
```

### **Paso 7: Crear Git Tag (2 minutos)**

```bash
git tag -a v1.1.0 -m "Release 1.1.0 - Kotlin DSL Complete"
git push origin v1.1.0
```

### **Paso 8: Deploy a Maven Central (10 minutos)**

```bash
# Limpiar y construir
mvn clean

# Deploy (te va a pedir la passphrase de GPG)
mvn clean deploy -P release

# Si todo sale bien, vas a ver:
# [INFO] BUILD SUCCESS
```

### **Paso 9: Verificar en Nexus (2 minutos)**

1. Ir a https://s01.oss.sonatype.org
2. Loguearse
3. Ir a "Staging Repositories"
4. Buscar `comadrewdev-xxxx`
5. Click en "Close" (esperar 1-2 minutos)
6. Click en "Release" (esperar 10-15 minutos)

### **Paso 10: Verificar en Maven Central (15 minutos)**

Después de liberar:

1. Ir a https://search.maven.org
2. Buscar `java-functional-extensions`
3. ¡Debería aparecer tu versión 1.1.0!

---

## 📦 **Cómo Usar tu Biblioteca**

### **Maven:**

```xml
<dependency>
    <groupId>com.adrewdev</groupId>
    <artifactId>java-functional-extensions</artifactId>
    <version>1.1.0</version>
</dependency>
```

### **Gradle:**

```kotlin
dependencies {
    implementation("com.adrewdev:java-functional-extensions:1.1.0")
}
```

### **Gradle Kotlin DSL:**

```kotlin
dependencies {
    implementation("com.adrewdev:java-functional-extensions:1.1.0")
}
```

---

## ⚠️ **Solución de Problemas**

### **Error: "Unauthorized"**

- Verificar `~/.m2/settings.xml` con credenciales correctas
- Las credenciales son del "User Token", NO de tu login

### **Error: "Missing signature"**

- Verificar que GPG está instalado
- Ejecutar: `mvn clean deploy -P release -Dgpg.passphrase=TU_PASSPHRASE`

### **Error: "Invalid POM"**

- Verificar que todos los campos en pom.xml están completos
- License, developer, scm URLs deben ser válidos

### **Error: "Repository not found"**

- Esperar 24-48 horas después de crear el issue
- Verificar que el issue fue aprobado

---

## 🎯 **Timeline Estimado**

| Paso | Tiempo |
|------|--------|
| Crear cuenta Sonatype | 5 min |
| Crear issue namespace | 10 min |
| Esperar aprobación | 24-48 hs |
| Configurar credenciales | 5 min |
| Configurar Maven settings | 5 min |
| Generar GPG key | 10 min |
| Subir a GitHub | 5 min |
| Crear tag | 2 min |
| Deploy | 10 min |
| Release en Nexus | 15 min |
| **TOTAL (sin espera)** | **~1 hora** |
| **TOTAL (con espera)** | **2-3 días** |

---

## 📝 **Checklist Final**

- [ ] Cuenta en Sonatype creada
- [ ] Issue OSSRH creado y aprobado
- [ ] Credenciales configuradas en `~/.m2/settings.xml`
- [ ] GPG key generada y exportada
- [ ] Proyecto en GitHub
- [ ] Tag v1.1.0 creado
- [ ] `mvn clean deploy -P release` exitoso
- [ ] Repositorio cerrado y liberado en Nexus
- [ ] Verificado en Maven Central

---

## 🔗 **Recursos Útiles**

- Sonatype OSSRH Guide: https://central.sonatype.org/pages/ossrh-guide.html
- Maven Deploy: https://maven.apache.org/plugins/maven-deploy-plugin/
- GPG Quick Start: https://www.gnupg.org/gph/en/manual/x110.html

---

## 🎉 **¡Una vez Publicado!**

- Actualizar README con badge de Maven Central
- Compartir en redes sociales
- Actualizar documentación
- ¡Celebrar! 🍾

---

**¿Tenés alguna duda sobre algún paso?**
